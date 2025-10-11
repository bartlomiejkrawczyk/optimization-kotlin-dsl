package io.github.bartlomiejkrawczyk.expression

data class LinearExpression(
    override val coefficients: Map<VariableName, Double> = emptyMap(),
    override val constant: Double = 0.0,
) : Expression
