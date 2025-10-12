package io.github.bartlomiejkrawczyk.linearsolver.constraint

import io.github.bartlomiejkrawczyk.linearsolver.OptimizerDslMarker
import io.github.bartlomiejkrawczyk.linearsolver.expression.BooleanVariable
import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression
import io.github.bartlomiejkrawczyk.linearsolver.expression.LinearExpression
import io.github.bartlomiejkrawczyk.linearsolver.model.OptimizerExtensions

/**
 * Builder for creating named linear constraints in the optimization model.
 *
 * Supports:
 * - Expression comparisons: `le`, `ge`, `eq`
 * - Boolean logic: `not`, `and`, `or`, `xor`, `allOf`, `noneOf`, `nOf`, `atLeast`, `atMost`
 *
 * Example:
 * ```kotlin
 * constraint {
 *     x1 + 2*x2 le 10
 * }
 *
 * "logic constraint - 1" {
 *     boolVar1 and boolVar2
 * }
 *
 * "logic constraint - 2" {
 *     boolVar3 or boolVar4
 * }
 *
 * "numeric constraint" {
 *     x1 + 2*x2 le 3*x1 - 10
 * }
 * ```
 *
 * @param name Optional constraint name.
 */
@OptimizerDslMarker
public open class ConstraintBuilder(
    private val name: String? = null,
) : OptimizerExtensions {

    /**
     * Defines a "less than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 le 5 }
     * // -> x1 + 2*x2 <= 5
     *
     * constraint { totalCost le budget }
     * // -> totalCost <= budget
     * ```
     */
    public infix fun Expression.le(value: Number): Constraint {
        return Constraint(
            name = name,
            left = this@le,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.LESS_EQUALS,
        )
    }

    /**
     * Defines a "less than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 le 5 }
     * // -> x1 + 2*x2 <= 5
     *
     * constraint { totalCost le budget }
     * // -> totalCost <= budget
     * ```
     */
    public infix fun Expression.le(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = this@le,
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
    }

    /**
     * Defines a "less than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 le 5 }
     * // -> x1 + 2*x2 <= 5
     *
     * constraint { totalCost le budget }
     * // -> totalCost <= budget
     * ```
     */
    public infix fun Number.le(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = LinearExpression(constant = this@le.toDouble()),
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
    }

    /**
     * Defines an equality constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + x2 eq 5 }
     * // -> x1 + x2 = 5
     *
     * constraint { demand eq supply }
     * // -> demand = supply
     * ```
     */
    public infix fun Expression.eq(value: Number): Constraint {
        return Constraint(
            name = name,
            left = this@eq,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.EQUALS,
        )
    }

    /**
     * Defines an equality constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + x2 eq 5 }
     * // -> x1 + x2 = 5
     *
     * constraint { demand eq supply }
     * // -> demand = supply
     * ```
     */
    public infix fun Expression.eq(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = this@eq,
            right = other,
            relationship = Relationship.EQUALS,
        )
    }

    /**
     * Defines an equality constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + x2 eq 5 }
     * // -> x1 + x2 = 5
     *
     * constraint { demand eq supply }
     * // -> demand = supply
     * ```
     */
    public infix fun Number.eq(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = LinearExpression(constant = this@eq.toDouble()),
            right = other,
            relationship = Relationship.EQUALS,
        )
    }

    /**
     * Defines a "greater than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 ge 10 }
     * // -> x1 + 2*x2 >= 10
     *
     * constraint { revenue ge cost + 1000 }
     * // -> revenue >= cost + 1000
     * ```
     */
    public infix fun Expression.ge(value: Number): Constraint {
        return Constraint(
            name = name,
            left = this@ge,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.GREATER_EQUALS,
        )
    }

    /**
     * Defines a "greater than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 ge 10 }
     * // -> x1 + 2*x2 >= 10
     *
     * constraint { revenue ge cost + 1000 }
     * // -> revenue >= cost + 1000
     * ```
     */
    public infix fun Expression.ge(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = this@ge,
            right = other,
            relationship = Relationship.GREATER_EQUALS,
        )
    }

    /**
     * Defines a "greater than or equal to" constraint between two expressions.
     *
     * Examples:
     * ```kotlin
     * constraint { x1 + 2*x2 ge 10 }
     * // -> x1 + 2*x2 >= 10
     *
     * constraint { revenue ge cost + 1000 }
     * // -> revenue >= cost + 1000
     * ```
     */
    public infix fun Number.ge(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = LinearExpression(constant = this@ge.toDouble()),
            right = other,
            relationship = Relationship.GREATER_EQUALS,
        )
    }

    /**
     * Defines a constraint `y = 1 - x` representing logical NOT.
     */
    public fun not(x: BooleanVariable, y: BooleanVariable): Constraint {
        return y eq 1 - x
    }

    /**
     * Defines a negated equality constraint.
     *
     * Example:
     * ```kotlin
     * constraint { x1 notEq x2 }
     * // -> x1 != x2
     * ```
     */
    public infix fun BooleanVariable.notEq(other: BooleanVariable): Constraint {
        return other eq 1 - this@notEq
    }

    /**
     * Defines AND constraints for two boolean variables.
     */
    public fun and(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Constraint {
        return 2.0 eq first + second
    }

    /**
     * Defines AND constraints for two boolean variables.
     */
    @JvmName("infixAnd")
    public infix fun BooleanVariable.and(
        other: BooleanVariable,
    ): Constraint {
        return 2.0 eq this@and + other
    }

    /**
     * Defines AND constraints for multiple boolean variables.
     */
    public fun Array<BooleanVariable>.and(): Constraint {
        return this@and.sum() eq size
    }

    /**
     * Defines AND constraints for multiple boolean variables.
     */
    public fun Collection<BooleanVariable>.and(): Constraint {
        return this@and.sum() eq size
    }


    /**
     * Defines OR constraints for two boolean variables.
     */
    public fun or(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Constraint {
        return 1.0 le first + second
    }


    /**
     * Defines OR constraints for two boolean variables.
     */
    @JvmName("infixOr")
    public infix fun BooleanVariable.or(
        other: BooleanVariable,
    ): Constraint {
        return 1.0 le this@or + other
    }


    /**
     * Defines OR constraints for multiple boolean variables.
     */
    public fun Array<BooleanVariable>.or(): Constraint {
        return this@or.sum() ge 1
    }


    /**
     * Defines OR constraints for multiple boolean variables.
     */
    public fun Collection<BooleanVariable>.or(): Constraint {
        return this@or.sum() ge 1
    }

    /** Defines XOR constraint for two boolean variables: `x1 + x2 = 1`. */
    public fun xor(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Constraint {
        return first + second eq 1
    }

    /** Defines XOR constraint for two boolean variables: `x1 + x2 = 1`. */
    @JvmName("infixXor")
    public infix fun BooleanVariable.xor(
        other: BooleanVariable,
    ): Constraint {
        return this@xor + other eq 1
    }

    /** Constrains all variables to 1 (true). */
    @JvmName("allOfVararg")
    public fun allOf(vararg variables: BooleanVariable): Constraint {
        return variables.sum() eq variables.size
    }

    /** Constrains all variables to 1 (true). */
    public fun Array<BooleanVariable>.allOf(): Constraint {
        return this@allOf.sum() eq size
    }

    /** Constrains all variables to 1 (true). */
    public fun Collection<BooleanVariable>.allOf(): Constraint {
        return this@allOf.sum() eq size
    }

    /** Constrains all variables to 0 (false). */
    @JvmName("noneOfVararg")
    public fun noneOf(vararg variables: BooleanVariable): Constraint {
        return variables.sum() eq 0
    }

    /** Constrains all variables to 0 (false). */
    public fun Array<BooleanVariable>.noneOf(): Constraint {
        return this@noneOf.sum() eq 0
    }

    /** Constrains all variables to 0 (false). */
    public fun Iterable<BooleanVariable>.noneOf(): Constraint {
        return this@noneOf.sum() eq 0
    }

    /** Enforces exactly `n` variables to be 1. */
    @JvmName("nOfVararg")
    public fun nOf(n: Int, vararg variables: BooleanVariable): Constraint {
        return variables.sum() eq n
    }

    /** Enforces exactly `n` variables to be 1. */
    public fun Array<BooleanVariable>.nOf(n: Int): Constraint {
        return this@nOf.sum() eq n
    }

    /** Enforces exactly `n` variables to be 1. */
    public fun Iterable<BooleanVariable>.nOf(n: Int): Constraint {
        return this@nOf.sum() eq n
    }

    /** Enforces at least `n` variables to be 1. */
    @JvmName("atLeastVararg")
    public fun atLeast(n: Int, vararg variables: BooleanVariable): Constraint {
        return variables.sum() ge n
    }

    /** Enforces at least `n` variables to be 1. */
    public fun Array<BooleanVariable>.atLeast(n: Int): Constraint {
        return this@atLeast.sum() ge n
    }

    /** Enforces at least `n` variables to be 1. */
    public fun Iterable<BooleanVariable>.atLeast(n: Int): Constraint {
        return this@atLeast.sum() ge n
    }

    /** Enforces at most `n` variables to be 1. */
    @JvmName("atMostVararg")
    public fun atMost(n: Int, vararg variables: BooleanVariable): Constraint {
        return variables.sum() le n
    }

    /** Enforces at most `n` variables to be 1. */
    public fun Array<BooleanVariable>.atMost(n: Int): Constraint {
        return this@atMost.sum() le n
    }

    /** Enforces at most `n` variables to be 1. */
    public fun Iterable<BooleanVariable>.atMost(n: Int): Constraint {
        return this@atMost.sum() le n
    }
}
