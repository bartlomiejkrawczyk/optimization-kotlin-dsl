package io.github.bartlomiejkrawczyk

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.model.SolverConfigurationBuilder

fun optimization(block: SolverConfigurationBuilder.() -> Unit): MPSolver.ResultStatus {
    val builder = SolverConfigurationBuilder()
    builder.block()
    val config = builder.build()
    return config.solver.solve()
}
