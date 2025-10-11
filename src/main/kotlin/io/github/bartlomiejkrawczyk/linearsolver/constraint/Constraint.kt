package io.github.bartlomiejkrawczyk.linearsolver.constraint

import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression

data class Constraint(
    // TODO: optional name?
    val left: Expression,
    val right: Expression,
    val relationship: Relationship,
)
