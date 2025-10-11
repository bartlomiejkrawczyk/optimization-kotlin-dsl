package io.github.bartlomiejkrawczyk.linearsolver

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfiguration
import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfigurationBuilder

fun optimization(block: SolverConfigurationBuilder.() -> Unit): Pair<MPSolver.ResultStatus, SolverConfiguration> {
    val builder = SolverConfigurationBuilder()
    builder.block()
    val config = builder.build()
    val status = config.solver.solve()
    // Verify that the solution satisfies all constraints (when using solvers
    // others than GLOP_LINEAR_PROGRAMMING, this is highly recommended!).
    config.solver.verifySolution(/* tolerance= */ builder.tolerance, /* log_errors= */ true)
    return status to config
}
