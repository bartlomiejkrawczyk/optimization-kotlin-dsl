package io.github.bartlomiejkrawczyk.linearsolver.expression

public data class Parameter(
    val name: VariableName,
    val coefficient: Double,
) : Expression {

    override val coefficients: Map<VariableName, Double>
        get() = mapOf(name to coefficient)

    override operator fun unaryMinus(): Parameter = copy(coefficient = -coefficient)

    override operator fun times(number: Number): Parameter =
        copy(coefficient = coefficient * number.toDouble())

    override operator fun div(number: Number): Parameter =
        copy(coefficient = coefficient / number.toDouble())

    public operator fun plus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to coefficient,
            ),
            constant = number.toDouble(),
        )
    }

    public operator fun plus(variable: Variable): Expression {
        if (variable.name == name) {
            return copy(coefficient = coefficient + 1.0)
        }
        return LinearExpression(
            coefficients = mapOf(
                name to coefficient,
                variable.name to 1.0,
            ),
        )
    }

    public operator fun plus(parameter: Parameter): Expression {
        if (parameter.name == name) {
            return copy(coefficient = coefficient + parameter.coefficient)
        }
        return LinearExpression(
            coefficients = mapOf(
                name to coefficient,
                parameter.name to parameter.coefficient,
            ),
        )
    }

    override operator fun plus(expression: Expression): LinearExpression {
        val newCoefficients = expression.coefficients.toMutableMap()
        newCoefficients[name] = newCoefficients.getOrDefault(name, 0.0) + coefficient
        return LinearExpression(
            coefficients = newCoefficients,
            constant = expression.constant,
        )
    }

    public operator fun minus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to coefficient,
            ),
            constant = -number.toDouble(),
        )
    }

    public operator fun minus(variable: Variable): Expression {
        if (variable.name == name) {
            return copy(coefficient = coefficient - 1.0)
        }
        return LinearExpression(
            coefficients = mapOf(
                name to coefficient,
                variable.name to -1.0,
            ),
        )
    }

    public operator fun minus(parameter: Parameter): Expression {
        if (parameter.name == name) {
            return copy(coefficient = coefficient - parameter.coefficient)
        }
        return LinearExpression(
            coefficients = mapOf(
                name to coefficient,
                parameter.name to -parameter.coefficient,
            ),
        )
    }

    override operator fun minus(expression: Expression): LinearExpression {
        val newCoefficients = expression.coefficients
            .mapValues { (_, coefficient) -> -coefficient }
            .toMutableMap()
        newCoefficients[name] = newCoefficients.getOrDefault(name, 0.0) + coefficient
        return LinearExpression(
            coefficients = newCoefficients,
            constant = -expression.constant,
        )
    }
}
