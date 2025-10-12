package io.github.bartlomiejkrawczyk.linearsolver.model

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPVariable
import io.github.bartlomiejkrawczyk.linearsolver.OptimizerDslMarker
import io.github.bartlomiejkrawczyk.linearsolver.constraint.Constraint
import io.github.bartlomiejkrawczyk.linearsolver.constraint.Relationship
import io.github.bartlomiejkrawczyk.linearsolver.constraint.StringConstraintBuilder
import io.github.bartlomiejkrawczyk.linearsolver.expression.*
import io.github.bartlomiejkrawczyk.linearsolver.objective.Goal
import io.github.bartlomiejkrawczyk.linearsolver.objective.Objective
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType

interface OptimizerExtensions {
    // Number extensions for building expressions

    infix fun Number.x(variable: Variable) = Parameter(coefficient = toDouble(), name = variable.name)

    operator fun Number.times(variable: Variable): Parameter =
        Parameter(coefficient = toDouble(), name = variable.name)

    operator fun Number.times(parameter: Parameter): Parameter =
        Parameter(coefficient = parameter.coefficient * toDouble(), name = parameter.name)

    operator fun Number.times(expression: Expression): LinearExpression =
        LinearExpression(
            constant = expression.constant * toDouble(),
            coefficients = expression.coefficients.mapValues { it.value * toDouble() },
        )

    operator fun Number.plus(variable: Variable): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(variable.name to 1.0),
        )
    }

    operator fun Number.plus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to parameter.coefficient),
        )
    }

    operator fun Number.plus(expression: Expression): LinearExpression {
        return LinearExpression(
            constant = expression.constant + toDouble(),
            coefficients = expression.coefficients,
        )
    }

    operator fun Number.minus(variable: Variable): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(variable.name to -1.0),
        )
    }

    operator fun Number.minus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to -parameter.coefficient),
        )
    }

    operator fun Number.minus(expression: Expression): LinearExpression {
        return LinearExpression(
            constant = toDouble() - expression.constant,
            coefficients = expression.coefficients.mapValues { -it.value },
        )
    }

    // Collection extensions
    fun <T : Expression> Array<T>.sum(): Expression {
        return reduce<Expression, Expression> { a, b -> a + b }
    }

    fun <T : Expression> Collection<T>.sum(): Expression {
        return reduce<Expression, Expression> { a, b -> a + b }
    }
}

@OptimizerDslMarker
class SolverConfigurationBuilder : OptimizerExtensions {

    var tolerance: Double = 1e-7

    var solver: SolverType? = SolverType.SCIP_MIXED_INTEGER_PROGRAMMING

    var sequence: Int = 1

    var objective: Objective? = null

    val variables = mutableMapOf<VariableName, Variable>()

    val constraints = mutableListOf<Constraint>()

    fun solver(type: SolverType) {
        this.solver = type
    }

    // Define variables

    fun intVar(
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

    fun numVar(
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

    fun boolVar(
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

    operator fun String.invoke(block: StringConstraintBuilder.() -> Constraint): Constraint {
        val builder = StringConstraintBuilder(this)
        val constraint = builder.block()
        constraints += constraint
        return constraint
    }

    fun constraint(
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

    infix fun Expression.le(value: Number): Constraint {
        val constraint = Constraint(
            left = this@le,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.LESS_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    infix fun Expression.le(other: Expression): Constraint {
        val constraint = Constraint(
            left = this@le,
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    infix fun Expression.eq(value: Number): Constraint {
        val constraint = Constraint(
            left = this@eq,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.EQUALS,
        )
        constraints += constraint
        return constraint
    }

    infix fun Expression.eq(other: Expression): Constraint {
        val constraint = Constraint(
            left = this@eq,
            right = other,
            relationship = Relationship.EQUALS,
        )
        constraints += constraint
        return constraint
    }

    infix fun Expression.ge(value: Number): Constraint {
        val constraint = Constraint(
            left = this@ge,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.GREATER_EQUALS,
        )
        constraints += constraint
        return constraint
    }

    infix fun Expression.ge(other: Expression): Constraint {
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

    infix fun Expression.to(goal: Goal): Objective {
        val newObjective = Objective(
            expression = this@to,
            goal = goal,
        )
        objective = newObjective
        return newObjective
    }

    infix fun min(expression: Expression): Objective {
        val newObjective = Objective(
            expression = expression,
            goal = Goal.MIN,
        )
        objective = newObjective
        return newObjective
    }

    infix fun max(expression: Expression): Objective {
        val newObjective = Objective(
            expression = expression,
            goal = Goal.MAX,
        )
        objective = newObjective
        return newObjective
    }


    // Builder method

    fun build(): SolverConfiguration {
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
