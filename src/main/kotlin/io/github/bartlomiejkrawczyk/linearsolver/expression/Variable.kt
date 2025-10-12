package io.github.bartlomiejkrawczyk.linearsolver.expression

import java.io.Serializable

@JvmInline
public value class VariableName(public val value: String) : Serializable

public interface Variable : Expression {
    public val name: VariableName

    override val coefficients: Map<VariableName, Double>
        get() = mapOf(name to 1.0)

    override operator fun unaryMinus(): Parameter = Parameter(name, -1.0)

    override operator fun times(number: Number): Expression {
        val value = number.toDouble()
        if (value == 0.0) {
            return LinearExpression()
        }
        return Parameter(this.name, value)
    }

    override operator fun div(number: Number): Parameter {
        return Parameter(this.name, 1.0 / number.toDouble())
    }

    public override operator fun plus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
            ),
            constant = number.toDouble(),
        )
    }

    public operator fun plus(variable: Variable): Expression {
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

    public operator fun plus(parameter: Parameter): Expression {
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

    public override operator fun minus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
            ),
            constant = -number.toDouble(),
        )
    }

    public operator fun minus(variable: Variable): Expression {
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

    public operator fun minus(parameter: Parameter): Expression {
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

public open class BooleanVariable(
    override val name: VariableName,
) : Variable

public open class IntegerVariable(
    override val name: VariableName,
    public val lowerBound: Double = Double.NEGATIVE_INFINITY,
    public val upperBound: Double = Double.POSITIVE_INFINITY,
) : Variable

public open class NumericVariable(
    override val name: VariableName,
    public val lowerBound: Double = Double.NEGATIVE_INFINITY,
    public val upperBound: Double = Double.POSITIVE_INFINITY,
) : Variable
