package io.github.bartlomiejkrawczyk.objective

import io.github.bartlomiejkrawczyk.expression.Expression

data class Objective(
    val expression: Expression,
    val goal: Goal,
)
