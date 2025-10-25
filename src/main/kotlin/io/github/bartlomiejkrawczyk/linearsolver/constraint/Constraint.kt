package io.github.bartlomiejkrawczyk.linearsolver.constraint

import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression

/**
 * Represents a linear constraint in the optimization model.
 *
 * Example:
 * ```kotlin
 * val c = Constraint(
 *     name = "capacity",
 *     left = x1 + 2*x2,
 *     right = 10,
 *     relationship = Relationship.LESS_EQUALS
 * )
 * // -> x1 + 2*x2 <= 10
 * ```
 *
 * @property name Optional name of the constraint.
 * @property left Left-hand side expression.
 * @property right Right-hand side expression.
 * @property relationship Type of relation (<=, =, >=).
 */
public data class Constraint(
    val name: String? = null,
    val left: Expression,
    val right: Expression,
    val relationship: Relationship,
    var constraint: MPConstraint? = null,
) {
    /**
     * Advanced usage: returns the dual value of the constraint in the current
     * solution (only available for continuous problems).
     */
    public val dualValue: Double
        get() =
            (constraint ?: throw IllegalStateException("Constraint not yet added to solver"))
                .dualValue()

    /**
     * Advanced usage: returns the basis status of the constraint.
     *
     * It is only available for continuous problems).
     *
     * Note that if a constraint `linear_expression in [lb, ub]` is transformed
     * into `linear_expression + slack = 0` with slack in `[-ub, -lb]`, then this
     * status is the same as the status of the slack variable with `AT_UPPER_BOUND`
     * and `AT_LOWER_BOUND` swapped.
     *
     * @see MPSolver#BasisStatus.
     */
    public val basisStatus: MPSolver.BasisStatus
        get() =
            (constraint ?: throw IllegalStateException("Constraint not yet added to solver"))
                .basisStatus()
}
