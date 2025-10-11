package io.github.bartlomiejkrawczyk.expression

import java.io.Serializable

@JvmInline
value class VariableName(val name: String) : Serializable

open class Variable(
    val name: VariableName,
) : Expression {

    override val coefficients: Map<VariableName, Double>
        get() = mapOf(name to 1.0)

    override operator fun unaryMinus(): Parameter = Parameter(name, -1.0)

    operator fun times(coefficient: Double): Parameter {
        return Parameter(this.name, coefficient)
    }

    operator fun plus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
            ),
            constant = number.toDouble(),
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
}

open class BooleanVariable(name: VariableName) : Variable(name)

open class IntegerVariable(
    name: VariableName,
    val lowerBound: Double = Double.NEGATIVE_INFINITY,
    val upperBound: Double = Double.POSITIVE_INFINITY,
) : Variable(name)

open class NumericVariable(
    name: VariableName,
    val lowerBound: Double = Double.NEGATIVE_INFINITY,
    val upperBound: Double = Double.POSITIVE_INFINITY,
) : Variable(name)
