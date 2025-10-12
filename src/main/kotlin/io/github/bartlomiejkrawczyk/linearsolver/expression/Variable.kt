package io.github.bartlomiejkrawczyk.linearsolver.expression

import java.io.Serializable

@JvmInline
value class VariableName(val value: String) : Serializable

interface Variable : Expression {
    val name: VariableName

    override val coefficients: Map<VariableName, Double>
        get() = mapOf(name to 1.0)

    override operator fun unaryMinus(): Parameter = Parameter(name, -1.0)

    override operator fun times(number: Number): Parameter {
        return Parameter(this.name, number.toDouble())
    }

    override operator fun div(number: Number): Parameter {
        return Parameter(this.name, 1.0 / number.toDouble())
    }

    operator fun plus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
            ),
            constant = number.toDouble(),
        )
    }

    operator fun plus(variable: Variable): Expression {
        if (variable.name == name) {
            return Parameter(coefficient = 2.0, name = name)
        }
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
                variable.name to 1.0,
            ),
        )
    }

    operator fun plus(parameter: Parameter): Expression {
        if (parameter.name == name) {
            return Parameter(coefficient = 1.0 + parameter.coefficient, name = name)
        }
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
                parameter.name to parameter.coefficient,
            ),
        )
    }

    override operator fun plus(expression: Expression): LinearExpression {
        val newCoefficients = expression.coefficients.toMutableMap()
        newCoefficients[name] = newCoefficients.getOrDefault(name, 0.0) + 1.0
        return LinearExpression(
            coefficients = newCoefficients,
            constant = expression.constant,
        )
    }

    operator fun minus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
            ),
            constant = -number.toDouble(),
        )
    }

    operator fun minus(variable: Variable): Expression {
        if (variable.name == name) {
            return LinearExpression()
        }
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
                variable.name to -1.0,
            ),
        )
    }

    operator fun minus(parameter: Parameter): Expression {
        if (parameter.name == name) {
            return Parameter(coefficient = 1.0 + parameter.coefficient, name = name)
        }
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
                parameter.name to -parameter.coefficient,
            ),
        )
    }

    override operator fun minus(expression: Expression): LinearExpression {
        val newCoefficients = expression.coefficients
            .mapValues { (_, coefficient) -> -coefficient }
            .toMutableMap()
        newCoefficients[name] = newCoefficients.getOrDefault(name, 0.0) + 1.0
        return LinearExpression(
            coefficients = newCoefficients,
            constant = -expression.constant,
        )
    }
}

open class BooleanVariable(
    override val name: VariableName,
) : Variable

open class IntegerVariable(
    override val name: VariableName,
    val lowerBound: Double = Double.NEGATIVE_INFINITY,
    val upperBound: Double = Double.POSITIVE_INFINITY,
) : Variable

open class NumericVariable(
    override val name: VariableName,
    val lowerBound: Double = Double.NEGATIVE_INFINITY,
    val upperBound: Double = Double.POSITIVE_INFINITY,
) : Variable
