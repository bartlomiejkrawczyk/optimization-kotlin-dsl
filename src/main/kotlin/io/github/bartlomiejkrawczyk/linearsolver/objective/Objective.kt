package io.github.bartlomiejkrawczyk.linearsolver.objective

import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression

/**
 * Represents an objective function for an optimization problem.
 *
 * Example:
 * ```kotlin
 * val objective = Objective(3*x1 + 2*x2, Goal.MAX)
 * // -> maximize 3*x1 + 2*x2
 * ```
 *
 * @property expression The linear expression being optimized.
 * @property goal The optimization direction (minimize or maximize).
 */
public data class Objective(
    val expression: Expression,
    val goal: Goal,
)
