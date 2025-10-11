package io.github.bartlomiejkrawczyk.linearsolver

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfigurationBuilder

fun optimization(block: SolverConfigurationBuilder.() -> Unit): MPSolver.ResultStatus {
    val builder = SolverConfigurationBuilder()
    builder.block()
    val config = builder.build()
    return config.solver.solve()
}
