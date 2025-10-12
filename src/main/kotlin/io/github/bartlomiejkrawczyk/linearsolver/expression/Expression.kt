package io.github.bartlomiejkrawczyk.linearsolver.expression

interface Expression {

    val coefficients: Map<VariableName, Double>

    val constant: Double
        get() = 0.0

    operator fun unaryMinus(): Expression =
        LinearExpression(
            coefficients.mapValues { -it.value },
            -constant
        )

    operator fun times(number: Number): Expression =
        LinearExpression(
            coefficients.mapValues { it.value * number.toDouble() },
            constant * number.toDouble()
        )

    operator fun div(number: Number): Expression =
        LinearExpression(
            coefficients.mapValues { it.value / number.toDouble() },
            constant / number.toDouble()
        )

    operator fun plus(expression: Expression): LinearExpression {
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

    operator fun minus(expression: Expression): LinearExpression {
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
