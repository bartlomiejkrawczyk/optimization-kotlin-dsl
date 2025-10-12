package io.github.bartlomiejkrawczyk.linearsolver.expression

import java.io.Serializable

/**
 * Represents the symbolic name of a decision variable in an optimization model.
 *
 * This is an inline value class for efficient storage and comparison.
 *
 * @property value The string name of the variable.
 */
@JvmInline
public value class VariableName(public val value: String) : Serializable

/**
 * Represents a variable term in an optimization expression.
 *
 * Variables are the building blocks of all optimization models.
 * They can participate in algebraic operations to form [LinearExpression]s and [Parameter]s.
 *
 * ### Example
 * ```kotlin
 * val x1 = numVar("x1")
 * val x2 = numVar("x2")
 *
 * // Linear combinations:
 * val expr1 = x1 + 2 * x2        // -> x1 + 2x2
 * val expr2 = 3 * x1 - 5         // -> 3x1 - 5
 * val expr3 = -x2 + 10           // -> -x2 + 10
 * ```
 *
 * These expressions can later be used to define constraints or objectives.
 */
public interface Variable : Expression {

    /** The unique name identifying this variable. */
    public val name: VariableName

    /**
     * Returns a map of coefficients where this variable has a coefficient of `1.0`.
     */
    override val coefficients: Map<VariableName, Double>
        get() = mapOf(name to 1.0)

    /**
     * Negates the variable, producing a [Parameter] with a coefficient of `-1.0`.
     */
    override operator fun unaryMinus(): Parameter = Parameter(name, -1.0)

    /**
     * Scales the variable by a numeric factor.
     *
     * Example:
     * ```kotlin
     * val x1 = numVar("x1")
     * val scaled = x1 * 3   // -> 3x1
     * ```
     *
     * @param number The scalar multiplier.
     * @return A [Parameter] representing the scaled variable.
     */
    override operator fun times(number: Number): Expression {
        val value = number.toDouble()
        if (value == 0.0) {
            return LinearExpression()
        }
        return Parameter(this.name, value)
    }

    /**
     * Divides the variable by a numeric divisor.
     *
     * Example:
     * ```kotlin
     * val x1 = numVar("x1")
     * val divided = x1 / 2  // -> 0.5x1
     * ```
     * @param number The divisor.
     * @return A [Parameter] representing the scaled variable.
     */
    override operator fun div(number: Number): Parameter {
        return Parameter(this.name, 1.0 / number.toDouble())
    }

    /**
     * Adds a numeric constant to the variable.
     *
     * Example:
     * ```kotlin
     * val x1 = numVar("x1")
     * val expr = x1 + 5     // -> x1 + 5
     * ```
     * @param number The constant to add.
     * @return A [LinearExpression] representing `variable + constant`.
     */
    public override operator fun plus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
            ),
            constant = number.toDouble(),
        )
    }

    /**
     * Adds another variable to this variable.
     *
     * Example:
     * ```kotlin
     * val expr = x1 + x2    // -> x1 + x2
     * ```
     * @param variable The variable to add.
     * @return A new [Expression] representing the sum of the two variables.
     */
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

    /**
     * Adds a parameter (a variable with coefficient) to this variable.
     *
     * Example:
     * ```kotlin
     * val p: Parameter = 2x2
     * val expr = x1 + p      // -> x1 + 2x2
     * ```
     * @param parameter The parameter to add.
     * @return A new [Expression] representing the sum.
     */
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

    /**
     * Adds another expression to this variable.
     *
     * Example:
     * ```kotlin
     * val e: LinearExpression = 2x2 + 2x3
     * val expr = x1 + e                   // -> x1 + (2x2 + 2x3)
     * ```
     * @param expression The right-hand side expression.
     * @return A [LinearExpression] combining coefficients and constants.
     */
    override operator fun plus(expression: Expression): LinearExpression {
        val newCoefficients = expression.coefficients.toMutableMap()
        newCoefficients[name] = newCoefficients.getOrDefault(name, 0.0) + 1.0
        return LinearExpression(
            coefficients = newCoefficients,
            constant = expression.constant,
        )
    }

    /**
     * Subtracts a numeric constant from this variable.
     *
     * Example:
     * ```kotlin
     * val x1 = numVar("x1")
     * val expr = x1 - 5     // -> x1 - 5
     * ```
     * @param number The constant to subtract.
     * @return A [LinearExpression] representing `variable - constant`.
     */
    public override operator fun minus(number: Number): LinearExpression {
        return LinearExpression(
            coefficients = mapOf(
                name to 1.0,
            ),
            constant = -number.toDouble(),
        )
    }

    /**
     * Subtracts another variable from this variable.
     *
     * Example:
     * ```kotlin
     * val expr = x1 - x2    // -> x1 - x2
     * ```
     * @param variable The variable to subtract.
     * @return A new [Expression] representing the difference.
     */
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

    /**
     * Subtracts a parameter (a variable with coefficient) from this variable.
     *
     * Example:
     * ```kotlin
     * val expr = x1 - 2x2    // -> x1 - 2x2
     * ```
     * @param parameter The parameter to subtract.
     * @return A new [Expression] representing the difference.
     */
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

    /**
     * Subtracts another expression from this variable.
     *
     * Example:
     * ```kotlin
     * val e: Expression = 2x2 + 2x3
     * val expr = x1 - e             // -> x1 - (2x2 + 2x3)
     * ```
     * @param expression The right-hand side expression.
     * @return A [LinearExpression] combining the terms.
     */
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

/**
 * Represents a binary (boolean) decision variable.
 *
 * Typically used for logical or selection-based decisions in mixed integer programming.
 *
 * Example:
 * ```kotlin
 * val y = boolVar("y")
 * // y ∈ {0, 1}
 * ```
 * @property name The unique name of this variable.
 */
public open class BooleanVariable(
    override val name: VariableName,
) : Variable

/**
 * Represents an integer decision variable with optional bounds.
 *
 * Example:
 * ```kotlin
 * val x1 = intVar("x1", lowerBound = 0, upperBound = 10)
 * // x1 ∈ [0, 10], integer
 * ```
 * @property name The unique name of this variable.
 * @property lowerBound The minimum allowable value.
 * @property upperBound The maximum allowable value.
 */
public open class IntegerVariable(
    override val name: VariableName,
    public val lowerBound: Double = Double.NEGATIVE_INFINITY,
    public val upperBound: Double = Double.POSITIVE_INFINITY,
) : Variable

/**
 * Represents a continuous (numeric) decision variable with optional bounds.
 *
 * Example:
 * ```kotlin
 * val x2 = numVar("x2", lowerBound = 0.0)
 * // x2 ≥ 0 (continuous)
 * ```
 * @property name The unique name of this variable.
 * @property lowerBound The minimum allowable value.
 * @property upperBound The maximum allowable value.
 */
public open class NumericVariable(
    override val name: VariableName,
    public val lowerBound: Double = Double.NEGATIVE_INFINITY,
    public val upperBound: Double = Double.POSITIVE_INFINITY,
) : Variable
