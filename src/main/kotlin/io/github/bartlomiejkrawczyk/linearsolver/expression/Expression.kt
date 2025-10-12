package io.github.bartlomiejkrawczyk.linearsolver.expression

/**
 * Represents a general algebraic expression composed of variables, coefficients, and a constant term.
 *
 * Expressions form the foundation of constraints and objective functions.
 * Implementations include [Variable], [Parameter], and [LinearExpression].
 *
 * ### Example
 * ```kotlin
 * val x1 = numVar("x1")
 * val x2 = numVar("x2")
 * val expr = (2 * x1 + 3 * x2) / 2 - 4 // -> x1 + 1.5x2 - 4
 * ```
 */
public interface Expression {

    /** The mapping of variable names to their coefficients in the expression. */
    public val coefficients: Map<VariableName, Double>

    /** The constant term in the expression (default is `0.0`). */
    public val constant: Double
        get() = 0.0

    /** Returns the negation of this expression.
     *
     * Example:
     * ```kotlin
     * val expr = -(x1 - 2x2) // -> -x1 + 2x2
     * ```
     */
    public operator fun unaryMinus(): Expression =
        LinearExpression(
            coefficients.mapValues { -it.value },
            -constant
        )

    /**
     * Multiplies this expression by a scalar value.
     *
     * Example:
     * ```kotlin
     * val scaled = (x1 - 2x2) * 3   // -> 3x1 - 6x2
     * ```
     * @param number The scalar multiplier.
     * @return A new [Expression] with scaled coefficients and constant.
     */
    public operator fun times(number: Number): Expression {
        val value = number.toDouble()
        if (value == 0.0) {
            return LinearExpression()
        }
        return LinearExpression(
            coefficients.mapValues { it.value * value },
            constant * value
        )
    }

    /**
     * Divides this expression by a scalar value.
     *
     * Example:
     * ```kotlin
     * val divided = (x1 - 2x2) / 2  // -> 0.5x1 - x2
     * ```
     * @param number The scalar divisor.
     * @return A new [Expression] with scaled coefficients and constant.
     */
    public operator fun div(number: Number): Expression =
        LinearExpression(
            coefficients.mapValues { it.value / number.toDouble() },
            constant / number.toDouble()
        )

    /**
     * Adds a constant value to this expression.
     *
     * Example:
     * ```kotlin
     * val expr = x1 - 2x2 + 5     // -> x1 - 2x2 + 5
     * ```
     * @param number The constant to add.
     * @return A new [LinearExpression] with the updated constant term.
     */
    public operator fun plus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = coefficients,
            constant = constant + number.toDouble(),
        )
    }

    /**
     * Adds another expression to this one.
     *
     * Example:
     * ```kotlin
     * val expr = x1 - 2x2 + x2    // -> x1 - x2
     * ```
     * @param expression The expression to add.
     * @return A [LinearExpression] combining coefficients and constants.
     */
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

    /**
     * Subtracts a constant from this expression.
     *
     * Example:
     * ```kotlin
     * val expr = x1 - 2x2 + 3 - 5     // -> x1 - 2x2 - 2
     * ```
     * @param number The constant to subtract.
     * @return A new [LinearExpression].
     */
    public operator fun minus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = coefficients,
            constant = constant - number.toDouble(),
        )
    }

    /**
     * Subtracts another expression from this one.
     *
     * Example:
     * ```kotlin
     * val expr = x1 - 2x2 + 3 - (x2 + 5)     // -> x1 - 3x2 - 2
     * ```
     * @param expression The right-hand side expression.
     * @return A [LinearExpression] representing the result.
     */
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
