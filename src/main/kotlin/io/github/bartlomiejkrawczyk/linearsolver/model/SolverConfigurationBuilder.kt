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

@Suppress("INAPPLICABLE_JVM_NAME")
public interface OptimizerExtensions {
    // Number extensions for building expressions

    public infix fun Number.x(value: Int): Parameter = Parameter(coefficient = toDouble(), name = VariableName("x$value"))

    public infix fun Number.x(name: String): Parameter = Parameter(coefficient = toDouble(), name = VariableName(name))

    public infix fun Number.x(variable: Variable): Parameter = Parameter(coefficient = toDouble(), name = variable.name)

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

    // TODO: maxmin?
    // TODO: minmax?
    // TODO: absolute?
}

@OptimizerDslMarker
public class SolverConfigurationBuilder : OptimizerExtensions {

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
        lowerBound: Double = Double.NEGATIVE_INFINITY,
        upperBound: Double = Double.POSITIVE_INFINITY,
    ): Variable {
        val variableName = name ?: "x${sequence++}"
        val variable = IntegerVariable(
            name = VariableName(variableName),
            lowerBound = lowerBound,
            upperBound = upperBound,
        )
        if (variables.containsKey(variable.name)) {
            throw IllegalArgumentException("Variable with name ${variable.name} already exists")
        }
        variables[variable.name] = variable
        return variable
    }

    public fun numVar(
        name: String? = null,
        lowerBound: Double = Double.NEGATIVE_INFINITY,
        upperBound: Double = Double.POSITIVE_INFINITY,
    ): Variable {
        val variableName = name ?: "x${sequence++}"
        val variable = NumericVariable(
            name = VariableName(variableName),
            lowerBound = lowerBound,
            upperBound = upperBound,
        )
        if (variables.containsKey(variable.name)) {
            throw IllegalArgumentException("Variable with name ${variable.name} already exists")
        }
        variables[variable.name] = variable
        return variable
    }

    public fun boolVar(
        name: String? = null,
    ): Variable {
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

    // TODO: configure array of variables
    // TODO: may add boolean operations on bool variables - eg. and, or, oneOf, allOf, nOf etc.

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
