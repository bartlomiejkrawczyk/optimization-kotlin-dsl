package io.github.bartlomiejkrawczyk.linearsolver

import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfigurationBuilder
import io.github.bartlomiejkrawczyk.linearsolver.objective.Solution

/**
 * Builds, solves, and validates an optimization model using the Kotlin DSL.
 *
 * Example:
 * ```kotlin
 * val result = optimization {
 *     val x1 = numVar("x1", lowerBound = 0.0)
 *     val x2 = numVar("x2", lowerBound = 0.0)
 *
 *     constraint { 3*x1 + 2*x2 le 10 }
 *     constraint { x1 + x2 ge 4 }
 *
 *     min(5*x1 + 4*x2)
 * }
 *
 * println("Status: ${result.status}")
 * println("Objective: ${result.objectiveValue}")
 * ```
 *
 * @param block The DSL block describing variables, constraints, and objective.
 * @return A [Solution] containing the solver results and configuration.
 */
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

/**
 * DSL marker for Kotlin optimization model builders.
 *
 * Ensures proper scoping in DSLs such as `optimization { ... }`.
 */
@DslMarker
public annotation class OptimizerDslMarker
