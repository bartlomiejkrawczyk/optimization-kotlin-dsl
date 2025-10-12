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

@Suppress("INAPPLICABLE_JVM_NAME")
public interface OptimizerExtensions {
    // Number extensions for building expressions

    public infix fun Number.x(value: Int): Parameter =
        Parameter(coefficient = toDouble(), name = VariableName("x$value"))

    public infix fun Number.x(name: String): Parameter =
        Parameter(coefficient = toDouble(), name = VariableName(name))

    public infix fun Number.x(variable: Variable): Parameter =
        Parameter(coefficient = toDouble(), name = variable.name)

    // TODO: may optimize multiplication by 0
    public operator fun Number.times(variable: Variable): Parameter =
        Parameter(coefficient = toDouble(), name = variable.name)

    public operator fun Number.times(parameter: Parameter): Parameter =
        Parameter(coefficient = parameter.coefficient * toDouble(), name = parameter.name)

    public operator fun Number.times(expression: Expression): LinearExpression =
        LinearExpression(
            constant = expression.constant * toDouble(),
            coefficients = expression.coefficients.mapValues { it.value * toDouble() },
        )

    public operator fun Number.plus(variable: Variable): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(variable.name to 1.0),
        )
    }

    public operator fun Number.plus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to parameter.coefficient),
        )
    }

    public operator fun Number.plus(expression: Expression): LinearExpression {
        return LinearExpression(
            constant = expression.constant + toDouble(),
            coefficients = expression.coefficients,
        )
    }

    public operator fun Number.minus(variable: Variable): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(variable.name to -1.0),
        )
    }

    public operator fun Number.minus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to -parameter.coefficient),
        )
    }

    public operator fun Number.minus(expression: Expression): LinearExpression {
        return LinearExpression(
            constant = toDouble() - expression.constant,
            coefficients = expression.coefficients.mapValues { -it.value },
        )
    }

    // Collection extensions
    @JvmName("sumArray")
    public fun <T : Expression> sum(vararg expressions: T): Expression {
        return expressions.reduce<Expression, Expression> { a, b -> a + b }
    }

    @JvmName("sumIterable")
    public fun <T : Expression> sum(expressions: Iterable<T>): Expression {
        return expressions.reduce<Expression, Expression> { a, b -> a + b }
    }

    public fun <T : Expression> Array<T>.sum(): Expression {
        return reduce<Expression, Expression> { a, b -> a + b }
    }

    public fun <T : Expression> Iterable<T>.sum(): Expression {
        return reduce<Expression, Expression> { a, b -> a + b }
    }

    @JvmName("avgArray")
    public fun <T : Expression> avg(vararg expressions: T): Expression {
        return expressions.sum() / expressions.size
    }

    @JvmName("avgCollection")
    public fun <T : Expression> avg(expressions: Collection<T>): Expression {
        return expressions.sum() / expressions.size
    }

    public fun <T : Expression> Array<T>.avg(): Expression {
        return sum() / size
    }

    public fun <T : Expression> Collection<T>.avg(): Expression {
        return sum() / size
    }
}

public fun <T> cartesianProduct(lists: List<List<T>>): Sequence<List<T>> {
    return lists.fold(sequenceOf(emptyList())) { acc, list ->
        acc.flatMap { partial -> list.asSequence().map { element -> partial + element } }
    }
}

@OptimizerDslMarker
public open class SolverConfigurationBuilder : OptimizerExtensions {

    public var tolerance: Double = 1e-7

    public var solver: SolverType? = SolverType.SCIP_MIXED_INTEGER_PROGRAMMING

    public var sequence: Int = 1

    public var objective: Objective? = null

    public val variables: MutableMap<VariableName, Variable> = mutableMapOf()

    public val constraints: MutableList<Constraint> = mutableListOf()

    public fun solver(type: SolverType) {
        this.solver = type
    }

    // Define variables

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

    public fun notVar(other: BooleanVariable, name: String? = null): Pair<BooleanVariable, Constraint> {
        val newVariable = boolVar(name = name)
        val constraint = constraint {
            newVariable eq 1 - other
        }
        return newVariable to constraint
    }

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
     * To make sure this var is minimal you have to minimize this value in the objective.
     */
    public fun maxVar(vararg expressions: Expression): Pair<Variable, List<Constraint>> {
        val newVariable = numVar()
        val constraints = expressions.map { expression ->
            constraint { newVariable ge expression }
        }
        return newVariable to constraints
    }

    /**
     * To make sure this var is minimal you have to maximize this value in the objective.
     */
    public fun minVar(vararg expressions: Expression): Pair<Variable, List<Constraint>> {
        val newVariable = numVar()
        val constraints = expressions.map { expression ->
            constraint { newVariable le expression }
        }
        return newVariable to constraints
    }

    /**
     * To make sure absolute value is correctly calculated you should include expressionToMinimize in your objective.
     *
     * In case of maximization, you should just maximize negative of the expression!
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

    // Boolean operations

    public fun not(x: BooleanVariable, y: BooleanVariable): Constraint {
        val constraint = constraint {
            y eq 1 - x
        }
        return constraint
    }

    public infix fun BooleanVariable.notEq(other: BooleanVariable): Constraint {
        val constraint = constraint {
            other eq 1 - this@notEq
        }
        return constraint
    }

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

    public fun xor(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Constraint {
        return constraint { first + second eq 1 }
    }

    @JvmName("infixXor")
    public infix fun BooleanVariable.xor(
        other: BooleanVariable,
    ): Constraint {
        return constraint { this@xor + other eq 1 }
    }

    @JvmName("allOfVararg")
    public fun allOf(vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() eq variables.size }
    }

    public fun Array<BooleanVariable>.allOf(): Constraint {
        return constraint { this@allOf.sum() eq size }
    }

    public fun Collection<BooleanVariable>.allOf(): Constraint {
        return constraint { this@allOf.sum() eq size }
    }

    @JvmName("noneOfVararg")
    public fun noneOf(vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() eq 0 }
    }

    public fun Array<BooleanVariable>.noneOf(): Constraint {
        return constraint { this@noneOf.sum() eq 0 }
    }

    public fun Iterable<BooleanVariable>.noneOf(): Constraint {
        return constraint { this@noneOf.sum() eq 0 }
    }

    @JvmName("nOfVararg")
    public fun nOf(n: Int, vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() eq n }
    }

    public fun Array<BooleanVariable>.nOf(n: Int): Constraint {
        return constraint { this@nOf.sum() eq n }
    }

    public fun Iterable<BooleanVariable>.nOf(n: Int): Constraint {
        return constraint { this@nOf.sum() eq n }
    }

    @JvmName("atLeastVararg")
    public fun atLeast(n: Int, vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() ge n }
    }

    public fun Array<BooleanVariable>.atLeast(n: Int): Constraint {
        return constraint { this@atLeast.sum() ge n }
    }

    public fun Iterable<BooleanVariable>.atLeast(n: Int): Constraint {
        return constraint { this@atLeast.sum() ge n }
    }

    @JvmName("atMostVararg")
    public fun atMost(n: Int, vararg variables: BooleanVariable): Constraint {
        return constraint { variables.sum() le n }
    }

    public fun Array<BooleanVariable>.atMost(n: Int): Constraint {
        return constraint { this@atMost.sum() le n }
    }

    public fun Iterable<BooleanVariable>.atMost(n: Int): Constraint {
        return constraint { this@atMost.sum() le n }
    }

    // Configure constraints

    public fun constraint(block: ConstraintBuilder.() -> Constraint): Constraint {
        val builder = ConstraintBuilder()
        val constraint = builder.block()
        constraints += constraint
        return constraint
    }

    public operator fun String.invoke(block: ConstraintBuilder.() -> Constraint): Constraint {
        val builder = ConstraintBuilder(this)
        val constraint = builder.block()
        constraints += constraint
        return constraint
    }

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

    // TODO: configure variable array operations!

    // Configure objective

    public fun min(block: OptimizerExtensions.() -> Expression): Objective {
        val builder = ObjectiveBuilder()
        val expression = builder.block()
        val newObjective = expression to Goal.MIN
        objective = newObjective
        return newObjective
    }

    public fun max(block: OptimizerExtensions.() -> Expression): Objective {
        val builder = ObjectiveBuilder()
        val expression = builder.block()
        val newObjective = expression to Goal.MAX
        objective = newObjective
        return newObjective
    }

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

    public fun objective(block: ObjectiveBuilder.() -> Objective): Objective {
        val builder = ObjectiveBuilder()
        val newObjective = builder.block()
        objective = newObjective
        return newObjective
    }

    public infix fun Expression.to(goal: Goal): Objective {
        val newObjective = Objective(
            expression = this@to,
            goal = goal,
        )
        objective = newObjective
        return newObjective
    }

    public infix fun min(expression: Expression): Objective {
        val newObjective = Objective(
            expression = expression,
            goal = Goal.MIN,
        )
        objective = newObjective
        return newObjective
    }

    public infix fun max(expression: Expression): Objective {
        val newObjective = Objective(
            expression = expression,
            goal = Goal.MAX,
        )
        objective = newObjective
        return newObjective
    }


    // Builder method

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
