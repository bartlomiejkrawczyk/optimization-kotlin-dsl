package io.github.bartlomiejkrawczyk.linearsolver.expression

public interface Expression {

    public val coefficients: Map<VariableName, Double>

    public val constant: Double
        get() = 0.0

    public operator fun unaryMinus(): Expression =
        LinearExpression(
            coefficients.mapValues { -it.value },
            -constant
        )

    public operator fun times(number: Number): Expression =
        LinearExpression(
            coefficients.mapValues { it.value * number.toDouble() },
            constant * number.toDouble()
        )

    public operator fun div(number: Number): Expression =
        LinearExpression(
            coefficients.mapValues { it.value / number.toDouble() },
            constant / number.toDouble()
        )

    public operator fun plus(expression: Expression): LinearExpression {
        val rightCoefficients = expression.coefficients
        val newCoefficients = coefficients.toMutableMap()
        for ((variable, coefficient) in rightCoefficients) {
            newCoefficients[variable] = newCoefficients.getOrDefault(variable, 0.0) + coefficient
        }
        return LinearExpression(
            coefficients = newCoefficients,
            constant = constant + expression.constant,
        )
    }

    public operator fun minus(expression: Expression): LinearExpression {
        val rightCoefficients = expression.coefficients
        val newCoefficients = coefficients.toMutableMap()
        for ((variable, coefficient) in rightCoefficients) {
            newCoefficients[variable] = newCoefficients.getOrDefault(variable, 0.0) - coefficient
        }
        return LinearExpression(
            coefficients = newCoefficients,
            constant = constant - expression.constant,
        )
    }
}
