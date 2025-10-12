package io.github.bartlomiejkrawczyk.linearsolver.objective

/**
 * Defines the optimization direction for the objective function.
 *
 * Example:
 * ```kotlin
 * val objective = Objective(expression = 3*x1 + 2*x2, goal = Goal.MIN)
 * // -> minimize 3*x1 + 2*x2
 * ```
 */
public enum class Goal {

    /** Minimize the objective. */
    MIN,

    /** Maximize the objective. */
    MAX,
}
