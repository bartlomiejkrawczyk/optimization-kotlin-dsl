package io.github.bartlomiejkrawczyk.constraint

import io.github.bartlomiejkrawczyk.expression.Expression

data class Constraint(
    val left: Expression,
    val right: Expression,
    val relationship: Relationship,
)
