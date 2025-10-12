package io.github.bartlomiejkrawczyk.linearsolver.objective

import io.github.bartlomiejkrawczyk.linearsolver.OptimizerDslMarker
import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression
import io.github.bartlomiejkrawczyk.linearsolver.model.OptimizerExtensions

/**
 * Builder for creating optimization objectives.
 *
 * Supports concise DSL-style definitions:
 * ```kotlin
 * objective { min(3*x1 + 2*x2) }
 * objective { max(x1 + x2) }
 * objective { (x1 + x2) to Goal.MAX }
 * ```
 */
@OptimizerDslMarker
public open class ObjectiveBuilder : OptimizerExtensions {

    /**
     * Creates an [Objective] from an expression and a [Goal].
     *
     * Example:
     * ```kotlin
     * val obj = (3*x1 + 2*x2) to Goal.MIN
     * // -> minimize 3*x1 + 2*x2
     * ```
     */
    public infix fun Expression.to(goal: Goal): Objective {
        return Objective(
            expression = this@to,
            goal = goal,
        )
    }

    /**
     * Creates a minimization objective directly from an expression.
     *
     * Example:
     * ```kotlin
     * val obj = min(3*x1 + 2*x2)
     * // -> minimize 3*x1 + 2*x2
     * ```
     */
    public infix fun min(expression: Expression): Objective {
        return Objective(
            expression = expression,
            goal = Goal.MIN,
        )
    }

    /**
     * Creates a maximization objective directly from an expression.
     *
     * Example:
     * ```kotlin
     * val obj = max(x1 + x2)
     * // -> maximize x1 + x2
     * ```
     */
    public infix fun max(expression: Expression): Objective {
        return Objective(
            expression = expression,
            goal = Goal.MAX,
        )
    }
}
