package io.github.bartlomiejkrawczyk.linearsolver.constraint

/**
 * Represents the type of relational operation in a constraint.
 *
 * Used to define whether a constraint is less-than, equal, or greater-than.
 *
 * Example:
 * ```kotlin
 * val constraint = Constraint(
 *     left = x1 + x2,
 *     right = 10,
 *     relationship = Relationship.LESS_EQUALS
 * )
 * // -> x1 + x2 <= 10
 * ```
 */
public enum class Relationship {

    /** Less than or equal (<=). */
    LESS_EQUALS,

    /** Equal to (=). */
    EQUALS,

    /** Greater than or equal (>=). */
    GREATER_EQUALS,
}
