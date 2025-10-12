package io.github.bartlomiejkrawczyk.linearsolver

import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfigurationBuilder
import io.github.bartlomiejkrawczyk.linearsolver.objective.Solution

public fun optimization(block: SolverConfigurationBuilder.() -> Unit): Solution {
    val builder = SolverConfigurationBuilder()
    builder.block()
    val config = builder.build()
    val status = config.solver.solve()
    // Verify that the solution satisfies all constraints (when using solvers
    // others than GLOP_LINEAR_PROGRAMMING, this is highly recommended!).
    config.solver.verifySolution(/* tolerance= */ builder.tolerance, /* log_errors= */ true)
    return Solution(
        status = status,
        config = config,
        builder = builder,
    )
}

@DslMarker
public annotation class OptimizerDslMarker
