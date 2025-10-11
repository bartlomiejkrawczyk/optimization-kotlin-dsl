package io.github.bartlomiejkrawczyk.linearsolver.model

import io.github.bartlomiejkrawczyk.linearsolver.constraint.Constraint
import io.github.bartlomiejkrawczyk.linearsolver.constraint.Relationship
import io.github.bartlomiejkrawczyk.linearsolver.expression.BooleanVariable
import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression
import io.github.bartlomiejkrawczyk.linearsolver.expression.IntegerVariable
import io.github.bartlomiejkrawczyk.linearsolver.expression.LinearExpression
import io.github.bartlomiejkrawczyk.linearsolver.expression.NumericVariable
import io.github.bartlomiejkrawczyk.linearsolver.expression.Parameter
import io.github.bartlomiejkrawczyk.linearsolver.expression.Variable
import io.github.bartlomiejkrawczyk.linearsolver.expression.VariableName
import io.github.bartlomiejkrawczyk.linearsolver.objective.Goal
import io.github.bartlomiejkrawczyk.linearsolver.objective.Objective
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import kotlin.collections.plusAssign

class SolverConfigurationBuilder {

    private var solver: SolverType? = null

    private var sequence: Int = 1

    private var variables = mutableMapOf<VariableName, Variable>()

    private val constraints = mutableListOf<Constraint>()

    private var objective: Objective? = null

    fun solver(type: SolverType) {
        this.solver = type
    }

    // Define variables

    fun intVar(
        name: String?,
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
        name: String?,
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
        name: String?,
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

    // Configure constraints

    fun constraint(
        left: Expression,
        right: Expression,
        relationship: Relationship,
    ) {
        constraints plusAssign Constraint(
            left = left,
            right = right,
            relationship = relationship
        )
    }

    infix fun Expression.lessEquals(value: Number) {
        constraints plusAssign Constraint(
            left = this@lessEquals,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.LESS_EQUALS,
        )
    }

    infix fun Expression.lessEquals(other: Expression) {
        constraints plusAssign Constraint(
            left = this@lessEquals,
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
    }

    infix fun Expression.equals(value: Number) {
        constraints plusAssign Constraint(
            left = this@equals,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.EQUALS,
        )
    }

    infix fun Expression.equals(other: Expression) {
        constraints plusAssign Constraint(
            left = this@equals,
            right = other,
            relationship = Relationship.EQUALS,
        )
    }

    infix fun Expression.greaterEquals(value: Number) {
        constraints plusAssign Constraint(
            left = this@greaterEquals,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.GREATER_EQUALS,
        )
    }

    infix fun Expression.greaterEquals(other: Expression) {
        constraints plusAssign Constraint(
            left = this@greaterEquals,
            right = other,
            relationship = Relationship.GREATER_EQUALS,
        )
    }

    // TODO: configure variable array operations!

    // Configure objective

    infix fun Expression.to(goal: Goal) {
        objective = Objective(
            expression = this@to,
            goal = goal,
        )
    }

    infix fun min(expression: Expression) {
        objective = Objective(
            expression = expression,
            goal = Goal.MIN,
        )
    }

    infix fun max(expression: Expression) {
        objective = Objective(
            expression = expression,
            goal = Goal.MAX,
        )
    }

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

    operator fun Number.minus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to -parameter.coefficient),
        )
    }

    // Builder method

    fun build(): SolverConfiguration {
        if (constraints.isEmpty()) {
            throw IllegalStateException("At least one linear constraint configuration must be provided")
        }
        if (objective == null) {
            throw IllegalStateException("Objective must be provided")
        }
        if (solver == null) {
            throw IllegalStateException("Choose solver")
        }
        return SolverConfiguration(
            solver = TODO(),
            constraints = TODO(),
            objective = TODO(),
            variables = TODO(),
        )
    }
}
