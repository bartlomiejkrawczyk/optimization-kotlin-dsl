package io.github.bartlomiejkrawczyk.linearsolver.constraint

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
)
