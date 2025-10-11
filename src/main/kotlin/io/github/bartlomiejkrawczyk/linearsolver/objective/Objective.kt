package io.github.bartlomiejkrawczyk.linearsolver.objective

import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression

data class Objective(
    val expression: Expression,
    val goal: Goal,
)
