package io.github.bartlomiejkrawczyk.linearsolver.objective

import com.google.ortools.linearsolver.MPObjective
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
    var objective: MPObjective? = null,
) {
    /**
     * Returns the objective value of the best solution found so far.
     *
     * It is the optimal objective value if the problem has been solved to
     * optimality.
     *
     * Note: the objective value may be slightly different than what you could
     * compute yourself using `MPVariable::solution_value();` please use the
     * --verify_solution flag to gain confidence about the numerical stability of
     * your solution.
     */
    public val value: Double
        get() =
            (objective ?: throw IllegalStateException("Objective not yet added to solver"))
                .value()

    /**
     * Returns the best objective bound.
     *
     * In case of minimization, it is a lower bound on the objective value of the
     * optimal integer solution. Only available for discrete problems.
     */
    public val bestBound: Double
        get() =
            (objective ?: throw IllegalStateException("Objective not yet added to solver"))
                .bestBound()

    override fun toString(): String {
        return buildString {
            when (goal) {
                Goal.MIN -> append("min: ")
                Goal.MAX -> append("max: ")
            }
            append(expression.toString())
        }
    }
}
