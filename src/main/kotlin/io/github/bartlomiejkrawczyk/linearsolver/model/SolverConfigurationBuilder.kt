package io.github.bartlomiejkrawczyk.linearsolver.model

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPVariable
import io.github.bartlomiejkrawczyk.linearsolver.OptimizerDslMarker
import io.github.bartlomiejkrawczyk.linearsolver.constraint.Constraint
import io.github.bartlomiejkrawczyk.linearsolver.constraint.ConstraintBuilder
import io.github.bartlomiejkrawczyk.linearsolver.constraint.Relationship
import io.github.bartlomiejkrawczyk.linearsolver.expression.*
import io.github.bartlomiejkrawczyk.linearsolver.objective.Goal
import io.github.bartlomiejkrawczyk.linearsolver.objective.Objective
import io.github.bartlomiejkrawczyk.linearsolver.objective.ObjectiveBuilder
import io.github.bartlomiejkrawczyk.linearsolver.objective.Solution
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import io.github.bartlomiejkrawczyk.linearsolver.tensor.NamedTensor
import io.github.bartlomiejkrawczyk.linearsolver.utils.OptimizerStringUtils.formatDouble

public fun <T> cartesianProduct(lists: List<List<T>>): Sequence<List<T>> {
    return lists.fold(sequenceOf(emptyList())) { acc, list ->
        acc.flatMap { partial -> list.asSequence().map { element -> partial + element } }
    }
}

/**
 * Builder class for configuring and constructing optimization problems.
 *
 * Provides a fluent DSL for defining decision variables, constraints, and objectives.
 *
 * Example:
 * ```kotlin
 * val solverConfig = SolverConfigurationBuilder().apply {
 *     val x1 = numVar("x1", lowerBound = 0.0)
 *     val x2 = numVar("x2", lowerBound = 0.0)
 *
 *     constraint { 3*x1 + 2*x2 le 10 }
 *     constraint { x1 + x2 ge 4 }
 *
 *     max { 5*x1 + 3*x2 }
 * }.build()
 * ```
 */
@OptimizerDslMarker
public open class SolverConfigurationBuilder : OptimizerExtensions {

    // -------------------------------------------------------------------------
    // Configuration properties
    // -------------------------------------------------------------------------

    /**
     * Numerical tolerance for solver convergence.
     * Default is `1e-7`.
     */
    public var tolerance: Double = 1e-7

    /**
     * The solver type to use for optimization (e.g., SCIP, CBC, GLOP).
     *
     * default: [SolverType.SCIP_MIXED_INTEGER_PROGRAMMING]
     */
    public var solver: SolverType? = SolverType.SCIP_MIXED_INTEGER_PROGRAMMING

    /**
     * The underlying OR-Tools solver instance.
     *
     * Initialized during the build process.
     */
    public var solverInstance: MPSolver? = null

    /**
     * The status of the last solve operation.
     */
    public var status: MPSolver.ResultStatus? = null

    /**
     * The number of threads to be used by the solver.
     */
    public var numThreads: Int? = null

    /**
     * Sequence number used for auto-generated variable names (x1, x2, ...).
     */
    public var sequence: Int = 1

    /**
     * The optimization objective (either MIN or MAX).
     */
    public var objective: Objective? = null

    /**
     * All registered variables in the model, keyed by their [VariableName].
     */
    public var variables: MutableMap<VariableName, Variable> = mutableMapOf()

    /** List of all constraints in the model. */
    public var constraints: MutableList<Constraint> = mutableListOf()

    /**
     * Sets the solver type.
     *
     * Example:
     * ```kotlin
     * solver(SolverType.GLOP_LINEAR_PROGRAMMING)
     * ```
     */
    public fun solver(type: SolverType) {
        this.solver = type
    }

    /**
     * Resets the builder state, clearing all variables, constraints, and objectives.
     */
    public fun clear() {
        solverInstance = null
        status = null
        sequence = 1
        objective = null
        variables = mutableMapOf()
        constraints = mutableListOf()
    }

    // -------------------------------------------------------------------------
    // Variable definitions
    // -------------------------------------------------------------------------

    /**
     * Defines an integer decision variable.
     *
     * Example:
     * ```kotlin
     * val x1 = intVar("x1", lowerBound = 0, upperBound = 10)
     * // -> 0 <= x1 <= 10 (integer)
     * ```
     */
    public fun intVar(
        name: String? = null,
        lowerBound: Number = Double.NEGATIVE_INFINITY,
        upperBound: Number = Double.POSITIVE_INFINITY,
    ): IntegerVariable {
        val variableName = name ?: "x${sequence++}"
        val variable = IntegerVariable(
            name = VariableName(variableName),
            lowerBound = lowerBound.toDouble(),
            upperBound = upperBound.toDouble(),
        )
        if (variables.containsKey(variable.name)) {
            throw IllegalArgumentException("Variable with name ${variable.name} already exists")
        }
        variables[variable.name] = variable
        return variable
    }

    /**
     * Defines a continuous (numeric) decision variable with optional bounds.
     *
     * Example:
     * ```kotlin
     * val x2 = numVar("x2", lowerBound = 0.0)
     * // -> x2 >= 0 (continuous)
     * ```
     */
    public fun numVar(
        name: String? = null,
        lowerBound: Number = Double.NEGATIVE_INFINITY,
        upperBound: Number = Double.POSITIVE_INFINITY,
    ): NumericVariable {
        val variableName = name ?: "x${sequence++}"
        val variable = NumericVariable(
            name = VariableName(variableName),
            lowerBound = lowerBound.toDouble(),
            upperBound = upperBound.toDouble(),
        )
        if (variables.containsKey(variable.name)) {
            throw IllegalArgumentException("Variable with name ${variable.name} already exists")
        }
        variables[variable.name] = variable
        return variable
    }

    /**
     * Defines a boolean (0-1) decision variable.
     *
     * Example:
     * ```kotlin
     * val y = boolVar("y")
     * // -> y ∈ {0, 1}
     * ```
     */
    public fun boolVar(
        name: String? = null,
    ): BooleanVariable {
        val variableName = name ?: "x${sequence++}"
        val variable = BooleanVariable(
            name = VariableName(variableName),
        )
        if (variables.containsKey(variable.name)) {
            throw IllegalArgumentException("Variable with name ${variable.name} already exists")
        }
        variables[variable.name] = variable
        return variable
    }

    // -------------------------------------------------------------------------
    // Vector and tensor variable creation
    // -------------------------------------------------------------------------

    /**
     * Creates a vector of boolean variables.
     *
     * Example:
     * ```kotlin
     * val items = vectorBoolVar(listOf("a", "b", "c"), namePrefix = "y")
     * // -> y_a, y_b, y_c ∈ {0, 1}
     * ```
     */
    public fun <T> vectorBoolVar(
        vectorKeys: List<T>,
        namePrefix: String? = null,
    ): NamedTensor<T, BooleanVariable> {
        return vectorVar(
            vectorKeys = vectorKeys,
            namePrefix = namePrefix,
            variableProvider = { name ->
                boolVar(
                    name = name,
                )
            }
        )
    }

    /**
     * Creates a vector of integer variables.
     *
     * Example:
     * ```kotlin
     * val x = vectorIntVar(listOf("a", "b"), namePrefix = "x", lowerBound = 0)
     * // -> x_a, x_b are integer >= 0
     * ```
     */
    public fun <T> vectorIntVar(
        vectorKeys: List<T>,
        namePrefix: String? = null,
        lowerBound: Number = Double.NEGATIVE_INFINITY,
        upperBound: Number = Double.POSITIVE_INFINITY,
    ): NamedTensor<T, IntegerVariable> {
        return vectorVar(
            vectorKeys = vectorKeys,
            namePrefix = namePrefix,
            variableProvider = { name ->
                intVar(
                    name = name,
                    lowerBound = lowerBound,
                    upperBound = upperBound,
                )
            }
        )
    }

    /**
     * Creates a vector of numeric variables.
     *
     * Example:
     * ```kotlin
     * val x = vectorNumVar(listOf("a", "b", "c"), namePrefix = "x", lowerBound = 0.0)
     * // -> x_a, x_b, x_c >= 0
     * ```
     */
    public fun <T> vectorNumVar(
        vectorKeys: List<T>,
        namePrefix: String? = null,
        lowerBound: Number = Double.NEGATIVE_INFINITY,
        upperBound: Number = Double.POSITIVE_INFINITY,
    ): NamedTensor<T, NumericVariable> {
        return vectorVar(
            vectorKeys = vectorKeys,
            namePrefix = namePrefix,
            variableProvider = { name ->
                numVar(
                    name = name,
                    lowerBound = lowerBound,
                    upperBound = upperBound,
                )
            }
        )
    }

    /**
     * Generic method to build a vector variable tensor using a custom provider.
     *
     * Example:
     * ```kotlin
     * val x = vectorVar(listOf("i1", "i2")) { name -> numVar(name, lowerBound = 0.0) }
     * ```
     */
    public fun <T, V : Variable> vectorVar(
        vectorKeys: List<T>,
        namePrefix: String? = null,
        variableProvider: (String) -> V,
    ): NamedTensor<T, V> {
        return tensorVar(
            tensorKeys = listOf(vectorKeys),
            namePrefix = namePrefix,
            variableProvider = variableProvider,
        )
    }

    /**
     * Creates a tensor (multi-dimensional array) of boolean variables.
     *
     * Example:
     * ```kotlin
     * val y = tensorBoolVar(listOf(listOf("i1", "i2"), listOf("j1", "j2")), namePrefix = "y")
     * // -> y_i1_j1, y_i1_j2,
     * //    y_i2_j1, y_i2_j2
     * ```
     */
    public fun <T> tensorBoolVar(
        tensorKeys: List<List<T>>,
        namePrefix: String? = null,
    ): NamedTensor<T, BooleanVariable> {
        return tensorVar(
            tensorKeys = tensorKeys,
            namePrefix = namePrefix,
            variableProvider = { name ->
                boolVar(
                    name = name,
                )
            }
        )
    }

    /**
     * Creates a tensor (multi-dimensional array) of integer variables.
     *
     * Example:
     * ```kotlin
     * val y = tensorIntVar(listOf(listOf("i1", "i2"), listOf("j1", "j2")), namePrefix = "y")
     * // -> y_i1_j1, y_i1_j2,
     * //    y_i2_j1, y_i2_j2
     * ```
     */
    public fun <T> tensorIntVar(
        tensorKeys: List<List<T>>,
        namePrefix: String? = null,
        lowerBound: Number = Double.NEGATIVE_INFINITY,
        upperBound: Number = Double.POSITIVE_INFINITY,
    ): NamedTensor<T, IntegerVariable> {
        return tensorVar(
            tensorKeys = tensorKeys,
            namePrefix = namePrefix,
            variableProvider = { name ->
                intVar(
                    name = name,
                    lowerBound = lowerBound,
                    upperBound = upperBound,
                )
            }
        )
    }

    /**
     * Creates a tensor (multi-dimensional array) of numeric variables.
     *
     * Example:
     * ```kotlin
     * val y = tensorNumVar(listOf(listOf("i1", "i2"), listOf("j1", "j2")), namePrefix = "y")
     * // -> y_i1_j1, y_i1_j2,
     * //    y_i2_j1, y_i2_j2
     * ```
     */
    public fun <T> tensorNumVar(
        tensorKeys: List<List<T>>,
        namePrefix: String? = null,
        lowerBound: Number = Double.NEGATIVE_INFINITY,
        upperBound: Number = Double.POSITIVE_INFINITY,
    ): NamedTensor<T, NumericVariable> {
        return tensorVar(
            tensorKeys = tensorKeys,
            namePrefix = namePrefix,
            variableProvider = { name ->
                numVar(
                    name = name,
                    lowerBound = lowerBound,
                    upperBound = upperBound,
                )
            }
        )
    }

    /**
     * Generic tensor builder for arbitrary variable types.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T, V : Variable> tensorVar(
        tensorKeys: List<List<T>>,
        namePrefix: String? = null,
        variableProvider: (String) -> V,
    ): NamedTensor<T, V> {
        val tensorVariables = mutableMapOf<T, Any>()

        for (tuple in cartesianProduct(tensorKeys)) {
            val variableName = (namePrefix ?: "x") + "_" + tuple.joinToString("_")

            var map: MutableMap<T, Any> = tensorVariables

            for (key in tuple.slice(0 until tuple.lastIndex)) {
                map = map.computeIfAbsent(key) { mutableMapOf<T, Any>() } as MutableMap<T, Any>
            }

            val variable = variableProvider(variableName)

            map[tuple.last()] = variable
        }

        return NamedTensor(
            keys = tensorKeys,
            values = tensorVariables,
        )
    }

    // -------------------------------------------------------------------------
    // Boolean combinators
    // -------------------------------------------------------------------------

    /**
     * Creates a boolean variable representing the logical NOT of a variable.
     *
     * Example:
     * ```kotlin
     * val (notY, constraint) = notVar(y)
     * // -> notY = 1 - y
     * ```
     */
    public fun notVar(other: BooleanVariable, name: String? = null): Pair<BooleanVariable, Constraint> {
        val newVariable = boolVar(name = name)
        val constraint = constraint {
            newVariable eq 1 - other
        }
        return newVariable to constraint
    }

    /**
     * Defines a variable representing logical AND of two boolean variables.
     *
     * Example:
     * ```kotlin
     * val (z, constraints) = andVar(x1, x2)
     * // -> z = x1 AND x2
     * ```
     */
    public fun andVar(
        first: BooleanVariable,
        second: BooleanVariable,
        name: String? = null,
    ): Pair<BooleanVariable, List<Constraint>> {
        val newVariable = boolVar(name = name)
        val constraints = listOf(
            constraint { newVariable le first },
            constraint { newVariable le second },
            constraint { newVariable ge first + second - 1 },
        )
        return newVariable to constraints
    }

    /**
     * Creates a boolean variable representing the logical AND of two or more variables.
     *
     * Example:
     * ```kotlin
     * val (z, constraints) = andVar(x1, x2, x3)
     * // -> z <= xi for all i
     * // -> z >= sum(xi) - (n - 1)
     * ```
     */
    public fun andVar(
        vararg variables: BooleanVariable,
        name: String? = null,
    ): Pair<BooleanVariable, List<Constraint>> {
        val newVariable = boolVar(name = name)
        val constraints = variables.map { variable ->
            constraint { newVariable le variable }
        }.toMutableList()
        constraints += constraint {
            newVariable ge variables.sum() - (variables.size - 1)
        }
        return newVariable to constraints
    }

    /**
     * Defines a variable representing logical OR of two boolean variables.
     *
     * Example:
     * ```kotlin
     * val (z, constraints) = orVar(x1, x2)
     * // -> z = x1 OR x2
     * ```
     */
    public fun orVar(
        first: BooleanVariable,
        second: BooleanVariable,
        name: String? = null,
    ): Pair<BooleanVariable, List<Constraint>> {
        val newVariable = boolVar(name = name)
        val constraints = listOf(
            constraint { newVariable ge first },
            constraint { newVariable ge second },
            constraint { newVariable le first + second },
        )
        return newVariable to constraints
    }

    /**
     * Creates a boolean variable representing the logical OR of two or more variables.
     *
     * Example:
     * ```kotlin
     * val (z, constraints) = orVar(x1, x2, x3)
     * // -> z >= xi for all i
     * // -> z <= sum(xi)
     * ```
     */
    public fun orVar(
        vararg variables: BooleanVariable,
        name: String? = null,
    ): Pair<BooleanVariable, List<Constraint>> {
        val newVariable = boolVar(name = name)
        val constraints = variables.map { variable ->
            constraint { newVariable ge variable }
        }.toMutableList()
        constraints += constraint {
            newVariable le variables.sum()
        }
        return newVariable to constraints
    }

    /**
     * Creates a boolean variable representing the logical XOR of two variables.
     *
     * Example:
     * ```kotlin
     * val (xorVar, andVar, constraints) = xorVars(x1, x2)
     * // -> xorVar = x1 + x2 - 2*andVar
     * ```
     */
    public fun xorVars(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Triple<BooleanVariable, BooleanVariable, List<Constraint>> {
        val (andVar, andConstraints) = andVar(first, second)
        val xorVar = boolVar()
        val xorConstraints = listOf(
            constraint { xorVar eq first + second - 2 * andVar }
        )
        return Triple(xorVar, andVar, (xorConstraints + andConstraints))
    }

    /**
     * Creates a variable representing the maximum among expressions.
     *
     * Note: To ensure this variable is minimized correctly, include it in the objective
     * (e.g., minimize this value or add it with a small epsilon weight).
     *
     * Example:
     * ```kotlin
     * val (maxVar, constraints) = maxVar(x1 + x2, x3)
     * // -> maxVar >= each expression
     * // minimize(maxVar)
     * ```
     */
    public fun maxVar(vararg expressions: Expression): Pair<Variable, List<Constraint>> {
        val newVariable = numVar()
        val constraints = expressions.map { expression ->
            constraint { newVariable ge expression }
        }
        return newVariable to constraints
    }

    /**
     * Creates a variable representing the minimum among expressions.
     *
     * Note: To ensure this variable is maximized correctly, include it in the objective
     * (e.g., maximize this value or add it with a small epsilon weight).
     *
     * Example:
     * ```kotlin
     * val (minVar, constraints) = minVar(x1 + x2, x3)
     * // -> minVar <= each expression
     * // maximize(minVar)
     * ```
     */
    public fun minVar(vararg expressions: Expression): Pair<Variable, List<Constraint>> {
        val newVariable = numVar()
        val constraints = expressions.map { expression ->
            constraint { newVariable le expression }
        }
        return newVariable to constraints
    }

    /**
     * Creates variables and constraint representing the absolute value of an expression.
     *
     * Note: To ensure the absolute value is minimized correctly, include the resulting expression
     * in the objective function. In case of maximization, maximize the negative of the expression.
     *
     * Example:
     * ```kotlin
     * val (pair, constraint, absExpr) = absoluteVar(x1 - x2)
     * // -> absExpr = |x1 - x2|
     * // minimize(absExpr)
     * ```
     */
    public fun absoluteVar(expression: Expression): Triple<Pair<Variable, Variable>, Constraint, Expression> {
        val positiveDeviation = numVar(lowerBound = 0)
        val negativeDeviation = numVar(lowerBound = 0)
        val constraint = constraint {
            positiveDeviation - negativeDeviation eq expression
        }
        val expressionToMinimize = positiveDeviation + negativeDeviation
        return Triple(
            positiveDeviation to negativeDeviation,
            constraint,
            expressionToMinimize,
        )
    }

    // -------------------------------------------------------------------------
    // Boolean constraint operations
    // -------------------------------------------------------------------------

    /**
     * Defines a constraint `x = 1` forcing true value for the variable.
     */
    public fun BooleanVariable.requireTrue(): Constraint {
        val x = this
        val constraint = constraint {
            x eq 1
        }
        return constraint
    }

    /**
     * Defines a constraint `x = 0` forcing false value for the variable.
     */
    public fun BooleanVariable.requireFalse(): Constraint {
        val x = this
        val constraint = constraint {
            x eq 0
        }
        return constraint
    }

    /**
     * Defines a constraint `y = 1 - x` representing logical NOT.
     */
    public fun not(x: BooleanVariable, y: BooleanVariable): Constraint {
        val constraint = constraint {
            y eq 1 - x
        }
        return constraint
    }

    /**
     * Defines a negated equality constraint.
     *
     * Example:
     * ```kotlin
     * constraint { x1 notEq x2 }
     * // -> x1 != x2
     * ```
     */
    public infix fun BooleanVariable.notEq(other: BooleanVariable): Constraint {
        val constraint = constraint {
            other eq 1 - this@notEq
        }
        return constraint
    }

    /**
     * Defines AND constraints for two boolean variables.
     */
    public fun and(
        first: BooleanVariable,
        second: BooleanVariable,
    ): List<Constraint> {
        val constraints = listOf(
            constraint { 1.0 le first },
            constraint { 1.0 le second },
            constraint { 1.0 ge first + second - 1 },
        )
        return constraints
    }

    /** Infix version of [and]. */
    @JvmName("infixAnd")
    public infix fun BooleanVariable.and(
        other: BooleanVariable,
    ): List<Constraint> {
        val constraints = listOf(
            constraint { 1.0 le this@and },
            constraint { 1.0 le other },
            constraint { 1.0 ge this@and + other - 1 },
        )
        return constraints
    }

    /** Defines OR constraints for two boolean variables. */
    public fun or(
        first: BooleanVariable,
        second: BooleanVariable,
    ): List<Constraint> {
        val constraints = listOf(
            constraint { 1.0 ge first },
            constraint { 1.0 ge second },
            constraint { 1.0 le first + second },
        )
        return constraints
    }

    /** Infix version of [or]. */
    @JvmName("infixOr")
    public infix fun BooleanVariable.or(
        other: BooleanVariable,
    ): List<Constraint> {
        val constraints = listOf(
            constraint { 1.0 ge this@or },
            constraint { 1.0 ge other },
            constraint { 1.0 le this@or + other },
        )
        return constraints
    }

    /** Defines XOR constraint for two boolean variables: x1 + x2 = 1. */
    public fun xor(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Constraint {
        return constraint { first + second eq 1 }
    }

    /** Infix version of [xor]. */
    @JvmName("infixXor")
    public infix fun BooleanVariable.xor(
        other: BooleanVariable,
    ): Constraint {
        return constraint { this@xor + other eq 1 }
    }

    /** Constrains all variables to 1 (true). */
    @JvmName("allOfVararg")
    public fun allOf(vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() eq variables.size }
    }

    /** Constrains all variables to 1 (true). */
    public fun Array<BooleanVariable>.allOf(): Constraint {
        return constraint { this@allOf.sum() eq size }
    }

    /** Constrains all variables to 1 (true). */
    public fun Collection<BooleanVariable>.allOf(): Constraint {
        return constraint { this@allOf.sum() eq size }
    }

    /** Constrains all variables to 0 (false). */
    @JvmName("noneOfVararg")
    public fun noneOf(vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() eq 0 }
    }

    /** Constrains all variables to 0 (false). */
    public fun Array<BooleanVariable>.noneOf(): Constraint {
        return constraint { this@noneOf.sum() eq 0 }
    }

    /** Constrains all variables to 0 (false). */
    public fun Iterable<BooleanVariable>.noneOf(): Constraint {
        return constraint { this@noneOf.sum() eq 0 }
    }

    /** Enforces exactly `n` variables to be 1. */
    @JvmName("nOfVararg")
    public fun nOf(n: Int, vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() eq n }
    }

    /** Enforces exactly `n` variables to be 1. */
    public fun Array<BooleanVariable>.nOf(n: Int): Constraint {
        return constraint { this@nOf.sum() eq n }
    }

    /** Enforces exactly `n` variables to be 1. */
    public fun Iterable<BooleanVariable>.nOf(n: Int): Constraint {
        return constraint { this@nOf.sum() eq n }
    }

    /** Enforces at least `n` variables to be 1. */
    @JvmName("atLeastVararg")
    public fun atLeast(n: Int, vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() ge n }
    }

    /** Enforces at least `n` variables to be 1. */
    public fun Array<BooleanVariable>.atLeast(n: Int): Constraint {
        return constraint { this@atLeast.sum() ge n }
    }

    /** Enforces at least `n` variables to be 1. */
    public fun Iterable<BooleanVariable>.atLeast(n: Int): Constraint {
        return constraint { this@atLeast.sum() ge n }
    }

    /** Enforces at most `n` variables to be 1. */
    @JvmName("atMostVararg")
    public fun atMost(n: Int, vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() le n }
    }

    /** Enforces at most `n` variables to be 1. */
    public fun Array<BooleanVariable>.atMost(n: Int): Constraint {
        return constraint { this@atMost.sum() le n }
    }

    /** Enforces at most `n` variables to be 1. */
    public fun Iterable<BooleanVariable>.atMost(n: Int): Constraint {
        return constraint { this@atMost.sum() le n }
    }

    // -------------------------------------------------------------------------
    // Constraint configuration
    // -------------------------------------------------------------------------

    /**
     * Creates and registers a constraint within the current model.
     *
     * Example:
     * ```kotlin
     * constraint { 3*x1 + 2*x2 le 10 }
     * ```
     */
    public fun constraint(block: ConstraintBuilder.() -> Constraint): Constraint {
        val builder = ConstraintBuilder()
        val constraint = builder.block()
        constraints += constraint
        return constraint
    }

    /**
     * DSL-style named constraint.
     *
     * Example:
     * ```kotlin
     * "capacity" {
     *     x1 + x2 le 100
     * }
     * ```
     */
    public operator fun String.invoke(block: ConstraintBuilder.() -> Constraint): Constraint {
        val builder = ConstraintBuilder(this)
        val constraint = builder.block()
        constraints += constraint
        return constraint
    }

    /** Creates a constraint between two [Expression]s. */
    public fun constraint(
        left: Expression,
        right: Expression,
        relationship: Relationship,
    ): Constraint {
        val constraint = Constraint(
            left = left,
            right = right,
            relationship = relationship
        )
        constraints += constraint
        return constraint
    }

    /**
     * Defines a "less than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 le 5 }
     * // -> x1 + 2*x2 <= 5
     *
     * constraint { totalCost le budget }
     * // -> totalCost <= budget
     * ```
     */
    public infix fun Expression.le(value: Number): Constraint {
        val constraint = Constraint(
            left = this@le,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.LESS_EQUALS,
        )
        constraints += constraint
        return constraint
    }


    /**
     * Defines a "less than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 le 5 }
     * // -> x1 + 2*x2 <= 5
     *
     * constraint { totalCost le budget }
     * // -> totalCost <= budget
     * ```
     */
    public infix fun Expression.le(other: Expression): Constraint {
        val constraint = Constraint(
            left = this@le,
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
        constraints += constraint
        return constraint
    }


    /**
     * Defines a "less than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 le 5 }
     * // -> x1 + 2*x2 <= 5
     *
     * constraint { totalCost le budget }
     * // -> totalCost <= budget
     * ```
     */
    public infix fun Number.le(other: Expression): Constraint {
        val constraint = Constraint(
            left = LinearExpression(constant = this@le.toDouble()),
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    /**
     * Defines an equality constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + x2 eq 5 }
     * // -> x1 + x2 = 5
     *
     * constraint { demand eq supply }
     * // -> demand = supply
     * ```
     */
    public infix fun Expression.eq(value: Number): Constraint {
        val constraint = Constraint(
            left = this@eq,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.EQUALS,
        )
        constraints += constraint
        return constraint
    }

    /**
     * Defines an equality constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + x2 eq 5 }
     * // -> x1 + x2 = 5
     *
     * constraint { demand eq supply }
     * // -> demand = supply
     * ```
     */
    public infix fun Expression.eq(other: Expression): Constraint {
        val constraint = Constraint(
            left = this@eq,
            right = other,
            relationship = Relationship.EQUALS,
        )
        constraints += constraint
        return constraint
    }

    /**
     * Defines an equality constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + x2 eq 5 }
     * // -> x1 + x2 = 5
     *
     * constraint { demand eq supply }
     * // -> demand = supply
     * ```
     */
    public infix fun Number.eq(other: Expression): Constraint {
        val constraint = Constraint(
            left = LinearExpression(constant = this@eq.toDouble()),
            right = other,
            relationship = Relationship.EQUALS,
        )
        constraints += constraint
        return constraint
    }

    /**
     * Defines a "greater than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 ge 10 }
     * // -> x1 + 2*x2 >= 10
     *
     * constraint { revenue ge cost + 1000 }
     * // -> revenue >= cost + 1000
     * ```
     */
    public infix fun Expression.ge(value: Number): Constraint {
        val constraint = Constraint(
            left = this@ge,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.GREATER_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    /**
     * Defines a "greater than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 ge 10 }
     * // -> x1 + 2*x2 >= 10
     *
     * constraint { revenue ge cost + 1000 }
     * // -> revenue >= cost + 1000
     * ```
     */
    public infix fun Expression.ge(other: Expression): Constraint {
        val constraint = Constraint(
            left = this@ge,
            right = other,
            relationship = Relationship.GREATER_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    /**
     * Defines a "greater than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 ge 10 }
     * // -> x1 + 2*x2 >= 10
     *
     * constraint { revenue ge cost + 1000 }
     * // -> revenue >= cost + 1000
     * ```
     */
    public infix fun Number.ge(other: Expression): Constraint {
        val constraint = Constraint(
            left = LinearExpression(constant = this@ge.toDouble()),
            right = other,
            relationship = Relationship.GREATER_EQUALS,
        )
        constraints += constraint
        return constraint
    }


    // -------------------------------------------------------------------------
    // Objective configuration
    // -------------------------------------------------------------------------

    /**
     * Maximizes the smallest value among given expressions (max-min problem).
     *
     * Example:
     * ```kotlin
     * val (obj, z, cons) = maxmin(x1, x2, x3)
     * // -> maximize z, where z <= x1, z <= x2, z <= x3
     * ```
     */
    public fun maxmin(vararg expressions: Expression): Triple<Objective, Variable, List<Constraint>> {
        val newVariable = numVar()
        val constraints = expressions.map { expression ->
            newVariable le expression
        }
        val objective = max {
            newVariable
        }
        return Triple(objective, newVariable, constraints)
    }

    /**
     * Minimizes the largest value among given expressions (min-max problem).
     *
     * Example:
     * ```kotlin
     * val (obj, z, cons) = minmax(x1, x2, x3)
     * // -> minimize z, where z >= x1, z >= x2, z >= x3
     * ```
     */
    public fun minmax(vararg expressions: Expression): Triple<Objective, Variable, List<Constraint>> {
        val newVariable = numVar()
        val constraints = expressions.map { expression ->
            newVariable ge expression
        }
        val objective = min {
            newVariable
        }
        return Triple(objective, newVariable, constraints)
    }

    /** Defines a custom objective directly via builder. */
    public fun objective(block: ObjectiveBuilder.() -> Objective): Objective {
        val builder = ObjectiveBuilder()
        val newObjective = builder.block()
        objective = newObjective
        return newObjective
    }

    /**
     * Creates an objective from an expression and a goal.
     *
     * Example:
     * ```kotlin
     * 3*x1 + 2*x2 to Goal.MIN
     * // -> minimize 3*x1 + 2*x2
     * ```
     */
    public infix fun Expression.to(goal: Goal): Objective {
        val newObjective = Objective(
            expression = this@to,
            goal = goal,
        )
        objective = newObjective
        return newObjective
    }

    /**
     * Sets a minimization objective.
     *
     * Example:
     * ```kotlin
     * min { 3*x1 + 2*x2 }
     * ```
     */
    public fun min(block: OptimizerExtensions.() -> Expression): Objective {
        val builder = ObjectiveBuilder()
        val expression = builder.block()
        val newObjective = expression to Goal.MIN
        objective = newObjective
        return newObjective
    }

    /**
     * Sets a minimization objective directly from an expression.
     *
     * Example:
     * ```kotlin
     * min(3*x1 + 2*x2)
     * // -> minimize 3*x1 + 2*x2
     * ```
     */
    public infix fun min(expression: Expression): Objective {
        val newObjective = Objective(
            expression = expression,
            goal = Goal.MIN,
        )
        objective = newObjective
        return newObjective
    }

    /**
     * Sets a maximization objective.
     *
     * Example:
     * ```kotlin
     * max { 5*x1 + 4*x2 }
     * ```
     */
    public fun max(block: OptimizerExtensions.() -> Expression): Objective {
        val builder = ObjectiveBuilder()
        val expression = builder.block()
        val newObjective = expression to Goal.MAX
        objective = newObjective
        return newObjective
    }

    /**
     * Sets a maximization objective directly from an expression.
     *
     * Example:
     * ```kotlin
     * max(x1 + x2)
     * // -> maximize x1 + x2
     * ```
     */
    public infix fun max(expression: Expression): Objective {
        val newObjective = Objective(
            expression = expression,
            goal = Goal.MAX,
        )
        objective = newObjective
        return newObjective
    }

    // -------------------------------------------------------------------------
    // Model building
    // -------------------------------------------------------------------------

    public override fun toString(): String {
        return buildString {
            appendLine("SolverConfigurationBuilder:")
            appendLine("  Solver Type: $solver")
            appendLine("  Tolerance: $tolerance")
            numThreads?.let {
                appendLine("  Num Threads: $it")
            }
            appendLine("  Objective:")
            appendLine("    $objective")
            appendLine("  Variables:")
            for (variable in variables.values) {
                when (variable) {
                    is BooleanVariable -> appendLine("    $variable")
                    is BoundedVariable -> {
                        val lbFinite = variable.lowerBound != Double.NEGATIVE_INFINITY
                        val ubFinite = variable.upperBound != Double.POSITIVE_INFINITY
                        val bounds = when {
                            lbFinite && ubFinite ->
                                "${formatDouble(variable.lowerBound)} <= $variable <= ${formatDouble(variable.upperBound)}"

                            lbFinite -> "${formatDouble(variable.lowerBound)} <= $variable"
                            ubFinite -> "$variable <= ${formatDouble(variable.upperBound)}"
                            else -> variable.toString()
                        }
                        appendLine("    $bounds")
                    }
                }
            }
            appendLine("  Constraints:")
            for (constraint in constraints) {
                appendLine("    $constraint")
            }
        }
    }

    public fun print() {
        println(this.toString())
    }

    /**
     * Builds and validates the solver configuration.
     *
     * Must contain:
     * - At least one variable
     * - At least one constraint
     * - An objective
     * - A solver type
     *
     * Example:
     * ```kotlin
     * val solverConfig = SolverConfigurationBuilder().apply {
     *     val x1 = numVar("x1", lowerBound = 0.0)
     *     val x2 = numVar("x2", lowerBound = 0.0)
     *
     *     constraint { 3*x1 + 2*x2 le 10 }
     *     constraint { x1 + x2 ge 4 }
     *
     *     max { 5*x1 + 3*x2 }
     * }.build()
     * ```
     */
    public fun build(): SolverConfiguration {
        if (constraints.isEmpty()) {
            throw IllegalStateException("At least one linear constraint configuration must be provided")
        }
        if (variables.isEmpty()) {
            throw IllegalStateException("At least one variable must be provided")
        }
        if (objective == null) {
            throw IllegalStateException("Objective must be provided")
        }
        if (solver == null) {
            throw IllegalStateException("Choose solver")
        }

        // load native libs once
        Loader.loadNativeLibraries()

        val solverInstance = MPSolver.createSolver(solver!!.name)
            ?: throw IllegalStateException("Could not create solver of type: $solver")
        this.solverInstance = solverInstance

        numThreads?.let { solverInstance.setNumThreads(it) }

        val solverVariables = mutableMapOf<VariableName, MPVariable>()

        for (variable in variables.values) {
            val solverVariable = when (variable) {
                is IntegerVariable -> {
                    solverInstance.makeIntVar(
                        variable.lowerBound,
                        variable.upperBound,
                        variable.name.value,
                    )
                }

                is NumericVariable -> {
                    solverInstance.makeNumVar(
                        variable.lowerBound,
                        variable.upperBound,
                        variable.name.value,
                    )
                }

                is BooleanVariable -> {
                    solverInstance.makeBoolVar(
                        variable.name.value,
                    )
                }

                else -> throw IllegalStateException("Unsupported variable type: ${variable::class}")
            }

            variable.variable = solverVariable

            solverVariables += variable.name to solverVariable
        }

        val solverConstraints = mutableListOf<MPConstraint>()

        for (constraint in constraints) {
            val expression = constraint.left - constraint.right
            val constant = -expression.constant

            var lowerBound = constant
            var upperBound = constant

            when (constraint.relationship) {
                Relationship.LESS_EQUALS -> {
                    lowerBound = Double.NEGATIVE_INFINITY
                }

                Relationship.GREATER_EQUALS -> {
                    upperBound = Double.POSITIVE_INFINITY
                }

                else -> {}
            }

            val solverConstraint = if (constraint.name != null) {
                solverInstance.makeConstraint(lowerBound, upperBound, constraint.name)
            } else {
                solverInstance.makeConstraint(lowerBound, upperBound)
            }
            constraint.constraint = solverConstraint

            expression.coefficients
                .filterValues { it != 0.0 }
                .mapKeys { (name, _) -> solverVariables[name] }
                .forEach { (variable, coefficient) -> solverConstraint.setCoefficient(variable, coefficient) }

            solverConstraints += solverConstraint
        }

        val solverObjective = solverInstance.objective()
        objective?.objective = solverObjective

        objective?.goal?.let { goal ->
            when (goal) {
                Goal.MIN -> solverObjective.setMinimization()
                Goal.MAX -> solverObjective.setMaximization()
            }
        }

        objective?.expression
            ?.coefficients
            ?.mapKeys { (name, _) -> solverVariables[name] }
            ?.forEach { (variable, coefficient) -> solverObjective.setCoefficient(variable, coefficient) }

        return SolverConfiguration(
            solver = solverInstance,
            constraints = solverConstraints,
            objective = solverObjective,
            variables = solverVariables.values.toList(),
        )
    }

    /**
     * Solves the optimization problem defined in this builder.
     *
     * Example:
     * ```kotlin
     * val solution = SolverConfigurationBuilder().apply {
     *     val x1 = numVar("x1", lowerBound = 0.0)
     *     val x2 = numVar("x2", lowerBound = 0.0)
     *
     *     constraint { 3*x1 + 2*x2 le 10 }
     *     constraint { x1 + x2 ge 4 }
     *
     *     max { 5*x1 + 3*x2 }
     *
     *     solver = SolverType.GLOP_LINEAR_PROGRAMMING
     * }.solve()
     */
    public fun solve(): Solution {
        val config = build()
        val status = config.solver.solve()
        this.status = status
        // Verify that the solution satisfies all constraints (when using solvers
        // others than GLOP_LINEAR_PROGRAMMING, this is highly recommended!).
        config.solver.verifySolution(/* tolerance= */ tolerance, /* log_errors= */ true)
        return Solution(
            status = status,
            config = config,
            builder = this,
        )
    }
}
