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
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import io.github.bartlomiejkrawczyk.linearsolver.tensor.NamedTensor

/**
 * Provides algebraic and collection extension functions for building linear expressions.
 *
 * This interface defines arithmetic and infix operators (`+`, `-`, `*`, `sum`, `avg`)
 * that allow concise, DSL-style formulation of optimization problems.
 *
 * Example:
 * ```kotlin
 * val x1 = numVar("x1")
 * val x2 = numVar("x2")
 *
 * val expr = 3 * x1 + 2 * x2 - 5 // -> 3*x1 + 2*x2 - 5
 * ```
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface OptimizerExtensions {

    // -------------------------------------------------------------------------
    // Number extensions for building expressions
    // -------------------------------------------------------------------------

    /**
     * Creates a [Parameter] with the given variable index, e.g. `3 x 1` → `3*x1`.
     *
     * Example:
     * ```kotlin
     * val term = 3 x 1 // -> 3*x1
     * ```
     */
    public infix fun Number.x(value: Int): Parameter =
        Parameter(coefficient = toDouble(), name = VariableName("x$value"))

    /**
     * Creates a [Parameter] with the given variable name.
     *
     * Example:
     * ```kotlin
     * val term = 5 x "x2" // -> 5*x2
     * ```
     */
    public infix fun Number.x(name: String): Parameter =
        Parameter(coefficient = toDouble(), name = VariableName(name))

    /**
     * Creates a [Parameter] from an existing [Variable].
     *
     * Example:
     * ```kotlin
     * val x3 = numVar("x3")
     * val term = 4 x x3 // -> 4*x3
     * ```
     */
    public infix fun Number.x(variable: Variable): Parameter =
        Parameter(coefficient = toDouble(), name = variable.name)

    /**
     * Multiplies this number by a [Variable] to produce an [Expression].
     *
     * Example:
     * ```kotlin
     * val expr = 2 * x1 // -> 2*x1
     * ```
     */
    public operator fun Number.times(variable: Variable): Expression {
        val value = toDouble()
        if (value == 0.0) {
            return LinearExpression()
        }
        return Parameter(coefficient = value, name = variable.name)
    }

    /**
     * Scales a [Parameter] by this number.
     *
     * Example:
     * ```kotlin
     * val term = 2 * (3*x2) // -> 6*x2
     * ```
     */
    public operator fun Number.times(parameter: Parameter): Expression {
        val value = toDouble()
        if (value == 0.0) {
            return LinearExpression()
        }
        return Parameter(coefficient = parameter.coefficient * toDouble(), name = parameter.name)
    }

    /**
     * Scales all coefficients and constants in an [Expression].
     *
     * Example:
     * ```kotlin
     * val expr = 2 * (x1 + x2 + 3) // -> 2*x1 + 2*x2 + 6
     * ```
     */
    public operator fun Number.times(expression: Expression): LinearExpression =
        LinearExpression(
            constant = expression.constant * toDouble(),
            coefficients = expression.coefficients.mapValues { it.value * toDouble() },
        )

    /**
     * Adds a [Variable] to a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 5 + x1 // -> x1 + 5
     * ```
     */
    public operator fun Number.plus(variable: Variable): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(variable.name to 1.0),
        )
    }

    /**
     * Adds a [Parameter] to a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 3 + (2*x2) // -> 2*x2 + 3
     * ```
     */
    public operator fun Number.plus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to parameter.coefficient),
        )
    }

    /**
     * Adds a numeric constant to an [Expression].
     *
     * Example:
     * ```kotlin
     * val expr = 10 + (3*x1 + 2*x2) // -> 3*x1 + 2*x2 + 10
     * ```
     */
    public operator fun Number.plus(expression: Expression): LinearExpression {
        return LinearExpression(
            constant = expression.constant + toDouble(),
            coefficients = expression.coefficients,
        )
    }

    /**
     * Subtracts a [Variable] from a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 10 - x1 // -> -x1 + 10
     * ```
     */
    public operator fun Number.minus(variable: Variable): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(variable.name to -1.0),
        )
    }

    /**
     * Subtracts a [Parameter] from a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 4 - (3 x "x2") // -> -3*x2 + 4
     * ```
     */
    public operator fun Number.minus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to -parameter.coefficient),
        )
    }

    /**
     * Subtracts an [Expression] from a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 5 - (x1 + 2*x2) // -> -x1 - 2*x2 + 5
     * ```
     */
    public operator fun Number.minus(expression: Expression): LinearExpression {
        return LinearExpression(
            constant = toDouble() - expression.constant,
            coefficients = expression.coefficients.mapValues { -it.value },
        )
    }

    // -------------------------------------------------------------------------
    // Collection extensions
    // -------------------------------------------------------------------------

    /**
     * Sums multiple [Expression]s.
     *
     * Example:
     * ```kotlin
     * val total = sum(3*x1, 2*x2, x3) // -> 3*x1 + 2*x2 + x3
     * ```
     */
    @JvmName("sumArray")
    public fun <T : Expression> sum(vararg expressions: T): Expression {
        return expressions.reduce<Expression, Expression> { a, b -> a + b }
    }

    /**
     * Sums all [Expression]s in an [Iterable].
     */
    @JvmName("sumIterable")
    public fun <T : Expression> sum(expressions: Iterable<T>): Expression {
        return expressions.reduce<Expression, Expression> { a, b -> a + b }
    }

    /**
     * Extension to sum all [Expression]s in an [Array].
     */
    public fun <T : Expression> Array<T>.sum(): Expression {
        return reduce<Expression, Expression> { a, b -> a + b }
    }

    /**
     * Extension to sum all [Expression]s in an [Iterable].
     */
    public fun <T : Expression> Iterable<T>.sum(): Expression {
        return reduce<Expression, Expression> { a, b -> a + b }
    }

    /**
     * Computes the average of given [Expression]s.
     *
     * Example:
     * ```kotlin
     * val avgExpr = avg(2*x1, 4*x2) // -> (2*x1 + 4*x2) / 2
     * ```
     */
    @JvmName("avgArray")
    public fun <T : Expression> avg(vararg expressions: T): Expression {
        return expressions.sum() / expressions.size
    }

    /**
     * Computes the average of all [Expression]s in a [Collection].
     */
    @JvmName("avgCollection")
    public fun <T : Expression> avg(expressions: Collection<T>): Expression {
        return expressions.sum() / expressions.size
    }

    /**
     * Extension to compute average of [Expression]s in an [Array].
     */
    public fun <T : Expression> Array<T>.avg(): Expression {
        return sum() / size
    }

    /**
     * Extension to compute average of [Expression]s in a [Collection].
     */
    public fun <T : Expression> Collection<T>.avg(): Expression {
        return sum() / size
    }
}

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

    /** Numerical tolerance used by solvers. Default is `1e-7`. */
    public var tolerance: Double = 1e-7

    /** Selected solver backend (default: [SolverType.SCIP_MIXED_INTEGER_PROGRAMMING]). */
    public var solver: SolverType? = SolverType.SCIP_MIXED_INTEGER_PROGRAMMING

    /** Sequential counter used for auto-naming variables (`x1`, `x2`, etc.). */
    public var sequence: Int = 1

    /** The objective function of the model. */
    public var objective: Objective? = null

    /** All registered variables in the model, keyed by their [VariableName]. */
    public val variables: MutableMap<VariableName, Variable> = mutableMapOf()

    /** List of all constraints in the model. */
    public val constraints: MutableList<Constraint> = mutableListOf()

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
     * // -> y_i1_j1, y_i1_j2, y_i2_j1, y_i2_j2
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
     * val y = tensorIntVar(listOf(listOf("i1", "i2"), listOf("j1", "j2")), namePrefix = "y")
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
     * Defines a variable representing logical NOT of another boolean variable.
     *
     * Example:
     * ```kotlin
     * val (notX, c) = notVar(x, name = "not_x")
     * // -> notX = 1 - x
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
     * Defines a variable representing AND across multiple boolean variables.
     *
     * Example:
     * ```kotlin
     * val (z, constraints) = andVar(x1, x2)
     * // -> z = x1 AND x2
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
     * Defines a variable representing OR across multiple boolean variables.
     *
     * Example:
     * ```kotlin
     * val (z, constraints) = orVar(x1, x2)
     * // -> z = x1 OR x2
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
     * Defines a variable representing XOR of two boolean variables.
     *
     * Example:
     * ```kotlin
     * val (xorVar, andVar, constraints) = xorVars(x1, x2)
     * // -> xorVar = x1 XOR x2
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
     * To make sure this var is minimal you have to minimize this value in the objective.
     *
     * Example:
     * ```kotlin
     * val (maxVar, constraints) = maxVar(x1 + x2, x3)
     * // -> maxVar >= each expression
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
     * Creates a variable representing the maximum among expressions.
     *
     * To make sure this var is minimal you have to maximize this value in the objective.
     *
     * Example:
     * ```kotlin
     * val (minVar, constraints) = minVar(x1 + x2, x3)
     * // -> minVar <= each expression
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
     * To make sure absolute value is correctly calculated you should include expressionToMinimize in your objective.
     *
     * In case of maximization, you should just maximize negative of the expression!
     *
     * Example:
     * ```kotlin
     * val (pair, c, absExpr) = absoluteVar(x1 - x2)
     * // -> absExpr = |x1 - x2|
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
     * Defines a constraint `y = 1 - x` representing logical NOT.
     */
    public fun not(x: BooleanVariable, y: BooleanVariable): Constraint {
        val constraint = constraint {
            y eq 1 - x
        }
        return constraint
    }

    /**
     * Infix version of logical NOT constraint.
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

    public infix fun Expression.le(value: Number): Constraint {
        val constraint = Constraint(
            left = this@le,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.LESS_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    public infix fun Expression.le(other: Expression): Constraint {
        val constraint = Constraint(
            left = this@le,
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    public infix fun Number.le(other: Expression): Constraint {
        val constraint = Constraint(
            left = LinearExpression(constant = this@le.toDouble()),
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    public infix fun Expression.eq(value: Number): Constraint {
        val constraint = Constraint(
            left = this@eq,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.EQUALS,
        )
        constraints += constraint
        return constraint
    }

    public infix fun Expression.eq(other: Expression): Constraint {
        val constraint = Constraint(
            left = this@eq,
            right = other,
            relationship = Relationship.EQUALS,
        )
        constraints += constraint
        return constraint
    }

    public infix fun Number.eq(other: Expression): Constraint {
        val constraint = Constraint(
            left = LinearExpression(constant = this@eq.toDouble()),
            right = other,
            relationship = Relationship.EQUALS,
        )
        constraints += constraint
        return constraint
    }

    public infix fun Expression.ge(value: Number): Constraint {
        val constraint = Constraint(
            left = this@ge,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.GREATER_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    public infix fun Expression.ge(other: Expression): Constraint {
        val constraint = Constraint(
            left = this@ge,
            right = other,
            relationship = Relationship.GREATER_EQUALS,
        )
        constraints += constraint
        return constraint
    }

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

    /** Infix versions for assigning goal: `expr to Goal.MIN` or `expr to Goal.MAX`. */
    public infix fun Expression.to(goal: Goal): Objective {
        val newObjective = Objective(
            expression = this@to,
            goal = goal,
        )
        objective = newObjective
        return newObjective
    }

    /** Infix minimization helper: `min(expr)`. */
    public infix fun min(expression: Expression): Objective {
        val newObjective = Objective(
            expression = expression,
            goal = Goal.MIN,
        )
        objective = newObjective
        return newObjective
    }

    /** Infix maximization helper: `max(expr)`. */
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

        val solverVariables = mutableMapOf<VariableName, MPVariable>()

        for (variable in variables.values) {
            when (variable) {
                is IntegerVariable -> {
                    solverVariables += variable.name to solverInstance.makeIntVar(
                        variable.lowerBound,
                        variable.upperBound,
                        variable.name.value,
                    )
                }

                is NumericVariable -> {
                    solverVariables += variable.name to solverInstance.makeNumVar(
                        variable.lowerBound,
                        variable.upperBound,
                        variable.name.value,
                    )
                }

                is BooleanVariable -> {
                    solverVariables += variable.name to solverInstance.makeBoolVar(
                        variable.name.value,
                    )
                }
            }
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

            expression.coefficients
                .filterValues { it != 0.0 }
                .mapKeys { (name, _) -> solverVariables[name] }
                .forEach { (variable, coefficient) -> solverConstraint.setCoefficient(variable, coefficient) }

            solverConstraints += solverConstraint
        }

        val solverObjective = solverInstance.objective()

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
}
