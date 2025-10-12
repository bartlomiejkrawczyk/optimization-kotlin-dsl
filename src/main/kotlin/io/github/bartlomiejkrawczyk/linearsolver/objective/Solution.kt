package io.github.bartlomiejkrawczyk.linearsolver.objective

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.model.OrToolsConfiguration
import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfiguration
import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfigurationBuilder

public data class Solution(
    public val status: MPSolver.ResultStatus,
    public val config: SolverConfiguration,
    public val builder: SolverConfigurationBuilder,
) : OrToolsConfiguration by config {

    val objectiveValue: Double
        get() = config.objective.value()

    public fun exportModelAsLpFormat(): String =
        config.solver.exportModelAsLpFormat()
}
