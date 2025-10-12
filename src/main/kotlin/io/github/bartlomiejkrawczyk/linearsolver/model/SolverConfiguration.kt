package io.github.bartlomiejkrawczyk.linearsolver.model

import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPObjective
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPVariable

/**
 * Concrete implementation of [OrToolsConfiguration].
 *
 * Holds a fully configured OR-Tools model: solver, variables, constraints,
 * and the objective function.
 *
 * Example:
 * ```kotlin
 * val solver = MPSolver.createSolver("SCIP")
 * val config = SolverConfiguration(
 *     solver = solver,
 *     variables = listOf(),
 *     constraints = listOf(),
 *     objective = solver.objective()
 * )
 * ```
 */
public open class SolverConfiguration(
    override val solver: MPSolver,
    override val variables: List<MPVariable>,
    override val constraints: List<MPConstraint>,
    override val objective: MPObjective,
) : OrToolsConfiguration
