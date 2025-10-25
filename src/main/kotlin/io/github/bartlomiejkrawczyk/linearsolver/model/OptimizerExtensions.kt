package io.github.bartlomiejkrawczyk.linearsolver.model

import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression
import io.github.bartlomiejkrawczyk.linearsolver.expression.LinearExpression
import io.github.bartlomiejkrawczyk.linearsolver.expression.Parameter
import io.github.bartlomiejkrawczyk.linearsolver.expression.Variable
import io.github.bartlomiejkrawczyk.linearsolver.expression.VariableName

/**
 * Provides algebraic and collection extension functions for building linear expressions.
 *
 * This interface defines arithmetic and infix operators (`+`, `-`, `*`, `sum`, `avg`)
 * that allow concise, DSL-style formulation of optimization problems.
 *
 * Example:
 * ```kotlin
 * val x1 = numVar("x1")
 * val x2 = numVar("x2")
 *
 * val expr = 3 * x1 + 2 * x2 - 5 // -> 3*x1 + 2*x2 - 5
 * ```
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface OptimizerExtensions {

    // -------------------------------------------------------------------------
    // Number extensions for building expressions
    // -------------------------------------------------------------------------

    /**
     * Creates a [Parameter] with the given variable index, e.g. `3 x 1` → `3*x1`.
     *
     * Example:
     * ```kotlin
     * val term = 3 x 1 // -> 3*x1
     * ```
     */
    public infix fun Number.x(value: Int): Parameter =
        Parameter(coefficient = toDouble(), name = VariableName("x$value"))

    /**
     * Creates a [Parameter] with the given variable name.
     *
     * Example:
     * ```kotlin
     * val term = 5 x "x2" // -> 5*x2
     * ```
     */
    public infix fun Number.x(name: String): Parameter =
        Parameter(coefficient = toDouble(), name = VariableName(name))

    /**
     * Creates a [Parameter] from an existing [Variable].
     *
     * Example:
     * ```kotlin
     * val x3 = numVar("x3")
     * val term = 4 x x3 // -> 4*x3
     * ```
     */
    public infix fun Number.x(variable: Variable): Parameter =
        Parameter(coefficient = toDouble(), name = variable.name)

    /**
     * Multiplies this number by a [Variable] to produce an [Expression].
     *
     * Example:
     * ```kotlin
     * val expr = 2 * x1 // -> 2*x1
     * ```
     */
    public operator fun Number.times(variable: Variable): Expression {
        val value = toDouble()
        if (value == 0.0) {
            return LinearExpression()
        }
        return Parameter(coefficient = value, name = variable.name)
    }

    /**
     * Scales a [Parameter] by this number.
     *
     * Example:
     * ```kotlin
     * val term = 2 * (3*x2) // -> 6*x2
     * ```
     */
    public operator fun Number.times(parameter: Parameter): Expression {
        val value = toDouble()
        if (value == 0.0) {
            return LinearExpression()
        }
        return Parameter(coefficient = parameter.coefficient * toDouble(), name = parameter.name)
    }

    /**
     * Scales all coefficients and constants in an [Expression].
     *
     * Example:
     * ```kotlin
     * val expr = 2 * (x1 + x2 + 3) // -> 2*x1 + 2*x2 + 6
     * ```
     */
    public operator fun Number.times(expression: Expression): LinearExpression =
        LinearExpression(
            constant = expression.constant * toDouble(),
            coefficients = expression.coefficients.mapValues { it.value * toDouble() },
        )

    /**
     * Adds a [Variable] to a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 5 + x1 // -> x1 + 5
     * ```
     */
    public operator fun Number.plus(variable: Variable): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(variable.name to 1.0),
        )
    }

    /**
     * Adds a [Parameter] to a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 3 + (2*x2) // -> 2*x2 + 3
     * ```
     */
    public operator fun Number.plus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to parameter.coefficient),
        )
    }

    /**
     * Adds a numeric constant to an [Expression].
     *
     * Example:
     * ```kotlin
     * val expr = 10 + (3*x1 + 2*x2) // -> 3*x1 + 2*x2 + 10
     * ```
     */
    public operator fun Number.plus(expression: Expression): LinearExpression {
        return LinearExpression(
            constant = expression.constant + toDouble(),
            coefficients = expression.coefficients,
        )
    }

    /**
     * Subtracts a [Variable] from a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 10 - x1 // -> -x1 + 10
     * ```
     */
    public operator fun Number.minus(variable: Variable): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(variable.name to -1.0),
        )
    }

    /**
     * Subtracts a [Parameter] from a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 4 - (3 x "x2") // -> -3*x2 + 4
     * ```
     */
    public operator fun Number.minus(parameter: Parameter): LinearExpression {
        return LinearExpression(
            constant = toDouble(),
            coefficients = mapOf(parameter.name to -parameter.coefficient),
        )
    }

    /**
     * Subtracts an [Expression] from a numeric constant.
     *
     * Example:
     * ```kotlin
     * val expr = 5 - (x1 + 2*x2) // -> -x1 - 2*x2 + 5
     * ```
     */
    public operator fun Number.minus(expression: Expression): LinearExpression {
        return LinearExpression(
            constant = toDouble() - expression.constant,
            coefficients = expression.coefficients.mapValues { -it.value },
        )
    }

    // -------------------------------------------------------------------------
    // Collection extensions
    // -------------------------------------------------------------------------

    /**
     * Sums multiple [Expression]s.
     *
     * Example:
     * ```kotlin
     * val total = sum(3*x1, 2*x2, x3) // -> 3*x1 + 2*x2 + x3
     * ```
     */
    @JvmName("sumArray")
    public fun <T : Expression> sum(vararg expressions: T): Expression {
        return expressions.reduce<Expression, Expression> { a, b -> a + b }
    }

    /**
     * Sums all [Expression]s in an [Iterable].
     */
    @JvmName("sumIterable")
    public fun <T : Expression> sum(expressions: Iterable<T>): Expression {
        return expressions.reduce<Expression, Expression> { a, b -> a + b }
    }

    /**
     * Extension to sum all [Expression]s in an [Array].
     */
    public fun <T : Expression> Array<T>.sum(): Expression {
        return reduce<Expression, Expression> { a, b -> a + b }
    }

    /**
     * Extension to sum all [Expression]s in an [Iterable].
     */
    public fun <T : Expression> Iterable<T>.sum(): Expression {
        return reduce<Expression, Expression> { a, b -> a + b }
    }

    /**
     * Computes the average of given [Expression]s.
     *
     * Example:
     * ```kotlin
     * val avgExpr = avg(2*x1, 4*x2) // -> (2*x1 + 4*x2) / 2
     * ```
     */
    @JvmName("avgArray")
    public fun <T : Expression> avg(vararg expressions: T): Expression {
        return expressions.sum() / expressions.size
    }

    /**
     * Computes the average of all [Expression]s in a [Collection].
     */
    @JvmName("avgCollection")
    public fun <T : Expression> avg(expressions: Collection<T>): Expression {
        return expressions.sum() / expressions.size
    }

    /**
     * Extension to compute average of [Expression]s in an [Array].
     */
    public fun <T : Expression> Array<T>.avg(): Expression {
        return sum() / size
    }

    /**
     * Extension to compute average of [Expression]s in a [Collection].
     */
    public fun <T : Expression> Collection<T>.avg(): Expression {
        return sum() / size
    }
}
