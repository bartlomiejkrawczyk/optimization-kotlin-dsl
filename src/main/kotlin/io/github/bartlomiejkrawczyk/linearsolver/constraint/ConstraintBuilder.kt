package io.github.bartlomiejkrawczyk.linearsolver.constraint

import io.github.bartlomiejkrawczyk.linearsolver.OptimizerDslMarker
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
}
