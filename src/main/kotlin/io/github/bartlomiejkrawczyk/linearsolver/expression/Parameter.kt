package io.github.bartlomiejkrawczyk.linearsolver.expression

/**
 * Represents a single variable with a scalar coefficient.
 *
 * A [Parameter] can participate in arithmetic operations and be combined into
 * larger [LinearExpression] objects.
 *
 * This is equivalent to a term like `3x1` or `-2x2`.
 *
 * ### Example
 * ```kotlin
 * val x1 = numVar("x1")
 * val p: Parameter = 3 * x1 // -> 3x1
 * val expr = p + 5          // -> 3x1 + 5
 * ```
 *
 * @property name The variable name.
 * @property coefficient The coefficient applied to the variable.
 */
public data class Parameter(
    val name: VariableName,
    val coefficient: Double,
) : Expression {

    /** Returns a map from variable name to its coefficient. */
    override val coefficients: Map<VariableName, Double>
        get() = mapOf(name to coefficient)

    /** Negates the parameter, flipping the sign of the coefficient. */
    override operator fun unaryMinus(): Parameter = copy(coefficient = -coefficient)

    /**
     * Multiplies this parameter by a scalar.
     *
     * Example:
     * ```kotlin
     * val scaled = 2x1 * 3   // -> 6x1
     * ```
     * @param number The multiplier.
     * @return A scaled [Parameter].
     */
    override operator fun times(number: Number): Expression {
        val value = number.toDouble()
        if (value == 0.0) {
            return LinearExpression()
        }
        return copy(coefficient = coefficient * value)
    }

    /**
     * Divides this parameter by a scalar.
     *
     * Example:
     * ```kotlin
     * val scaled = 2x1 / 4   // -> 0.5x1
     * ```
     * @param number The divisor.
     * @return A scaled [Parameter].
     */
    override operator fun div(number: Number): Parameter =
        copy(coefficient = coefficient / number.toDouble())

    /**
     * Adds a constant value to this parameter.
     *
     * Example:
     * ```kotlin
     * val expr = 2x1 + 5     // -> 2x1 + 5
     * ```
     * @param number The constant to add.
     * @return A [LinearExpression] with the constant added.
     */
    public override operator fun plus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to coefficient,
            ),
            constant = number.toDouble(),
        )
    }

    /**
     * Adds a variable to this parameter.
     *
     * Example:
     * ```kotlin
     * val expr = 2x1 + x2    // -> 2x1 + x2
     * ```
     * @param variable The variable to add.
     * @return A combined [Expression].
     */
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

    /**
     * Adds another parameter to this parameter.
     *
     * Example:
     * ```kotlin
     * val p: Parameter = 2x2
     * val expr = 2x1 + p      // -> 2x1 + 2x2
     * ```
     * @param parameter The parameter to add.
     * @return A combined [Expression].
     */
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

    /**
     * Adds another expression to this parameter.
     *
     * Example:
     * ```kotlin
     * val e: LinearExpression = 2x2 + 2x3
     * val expr = 2x1 + e                   // -> 2x1 + (2x2 + 2x3)
     * ```
     * @param expression The right-hand side expression.
     * @return A [LinearExpression] combining terms.
     */
    override operator fun plus(expression: Expression): LinearExpression {
        val newCoefficients = expression.coefficients.toMutableMap()
        newCoefficients[name] = newCoefficients.getOrDefault(name, 0.0) + coefficient
        return LinearExpression(
            coefficients = newCoefficients,
            constant = expression.constant,
        )
    }

    /**
     * Subtracts a constant value from this parameter.
     *
     * Example:
     * ```kotlin
     * val expr = 2x1 - 5     // -> 2x1 - 5
     * ```
     * @param number The constant to subtract.
     * @return A [LinearExpression].
     */
    public override operator fun minus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to coefficient,
            ),
            constant = -number.toDouble(),
        )
    }

    /**
     * Subtracts a variable from this parameter.
     *
     * Example:
     * ```kotlin
     * val expr = 2x1 - x2    // -> 2x1 - x2
     * ```
     * @param variable The variable to subtract.
     * @return A [Expression] representing the difference.
     */
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

    /**
     * Subtracts another parameter from this parameter.
     *
     * Example:
     * ```kotlin
     * val expr = 2x1 - 2x2    // -> 2x1 - 2x2
     * ```
     * @param parameter The parameter to subtract.
     * @return A new [Expression] representing the difference.
     */
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

    /**
     * Subtracts another expression from this parameter.
     *
     * Example:
     * ```kotlin
     * val e: Expression = 2x2 + 2x3
     * val expr = 2x1 - e            // -> 2x1 - (2x2 + 2x3)
     * ```
     * @param expression The expression to subtract.
     * @return A [LinearExpression] representing the difference.
     */
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
