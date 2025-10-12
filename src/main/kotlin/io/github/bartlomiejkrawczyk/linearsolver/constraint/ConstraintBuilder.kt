package io.github.bartlomiejkrawczyk.linearsolver.constraint

import io.github.bartlomiejkrawczyk.linearsolver.OptimizerDslMarker
import io.github.bartlomiejkrawczyk.linearsolver.expression.BooleanVariable
import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression
import io.github.bartlomiejkrawczyk.linearsolver.expression.LinearExpression
import io.github.bartlomiejkrawczyk.linearsolver.model.OptimizerExtensions

@OptimizerDslMarker
public open class ConstraintBuilder(
    private val name: String? = null,
) : OptimizerExtensions {

    public infix fun Expression.le(value: Number): Constraint {
        return Constraint(
            name = name,
            left = this@le,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.LESS_EQUALS,
        )
    }

    public infix fun Expression.le(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = this@le,
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
    }

    public infix fun Number.le(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = LinearExpression(constant = this@le.toDouble()),
            right = other,
            relationship = Relationship.LESS_EQUALS,
        )
    }

    public infix fun Expression.eq(value: Number): Constraint {
        return Constraint(
            name = name,
            left = this@eq,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.EQUALS,
        )
    }

    public infix fun Expression.eq(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = this@eq,
            right = other,
            relationship = Relationship.EQUALS,
        )
    }

    public infix fun Number.eq(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = LinearExpression(constant = this@eq.toDouble()),
            right = other,
            relationship = Relationship.EQUALS,
        )
    }

    public infix fun Expression.ge(value: Number): Constraint {
        return Constraint(
            name = name,
            left = this@ge,
            right = LinearExpression(constant = value.toDouble()),
            relationship = Relationship.GREATER_EQUALS,
        )
    }

    public infix fun Expression.ge(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = this@ge,
            right = other,
            relationship = Relationship.GREATER_EQUALS,
        )
    }

    public infix fun Number.ge(other: Expression): Constraint {
        return Constraint(
            name = name,
            left = LinearExpression(constant = this@ge.toDouble()),
            right = other,
            relationship = Relationship.GREATER_EQUALS,
        )
    }

    public fun not(x: BooleanVariable, y: BooleanVariable): Constraint {
        return y eq 1 - x
    }

    public infix fun BooleanVariable.notEq(other: BooleanVariable): Constraint {
        return other eq 1 - this@notEq
    }

    public fun and(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Constraint {
        return 2.0 eq first + second
    }

    @JvmName("infixAnd")
    public infix fun BooleanVariable.and(
        other: BooleanVariable,
    ): Constraint {
        return 2.0 eq this@and + other
    }

    public fun or(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Constraint {
        return 1.0 le first + second
    }

    @JvmName("infixOr")
    public infix fun BooleanVariable.or(
        other: BooleanVariable,
    ): Constraint {
        return 1.0 le this@or + other
    }

    public fun xor(
        first: BooleanVariable,
        second: BooleanVariable,
    ): Constraint {
        return first + second eq 1
    }

    @JvmName("infixXor")
    public infix fun BooleanVariable.xor(
        other: BooleanVariable,
    ): Constraint {
        return this@xor + other eq 1
    }

    @JvmName("allOfVararg")
    public fun allOf(vararg variables: BooleanVariable): Constraint {
        return variables.sum() eq variables.size
    }

    public fun Array<BooleanVariable>.allOf(): Constraint {
        return this@allOf.sum() eq size
    }

    public fun Collection<BooleanVariable>.allOf(): Constraint {
        return this@allOf.sum() eq size
    }

    @JvmName("noneOfVararg")
    public fun noneOf(vararg variables: BooleanVariable): Constraint {
        return variables.sum() eq 0
    }

    public fun Array<BooleanVariable>.noneOf(): Constraint {
        return this@noneOf.sum() eq 0
    }

    public fun Iterable<BooleanVariable>.noneOf(): Constraint {
        return this@noneOf.sum() eq 0
    }

    @JvmName("nOfVararg")
    public fun nOf(n: Int, vararg variables: BooleanVariable): Constraint {
        return variables.sum() eq n
    }

    public fun Array<BooleanVariable>.nOf(n: Int): Constraint {
        return this@nOf.sum() eq n
    }

    public fun Iterable<BooleanVariable>.nOf(n: Int): Constraint {
        return this@nOf.sum() eq n
    }

    @JvmName("atLeastVararg")
    public fun atLeast(n: Int, vararg variables: BooleanVariable): Constraint {
        return variables.sum() ge n
    }

    public fun Array<BooleanVariable>.atLeast(n: Int): Constraint {
        return this@atLeast.sum() ge n
    }

    public fun Iterable<BooleanVariable>.atLeast(n: Int): Constraint {
        return this@atLeast.sum() ge n
    }

    @JvmName("atMostVararg")
    public fun atMost(n: Int, vararg variables: BooleanVariable): Constraint {
        return variables.sum() le n
    }

    public fun Array<BooleanVariable>.atMost(n: Int): Constraint {
        return this@atMost.sum() le n
    }

    public fun Iterable<BooleanVariable>.atMost(n: Int): Constraint {
        return this@atMost.sum() le n
    }
}
