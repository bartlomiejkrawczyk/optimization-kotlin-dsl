package io.github.bartlomiejkrawczyk.linearsolver.model

import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPObjective
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPVariable

/**
 * Represents the configuration of an OR-Tools solver instance.
 *
 * This interface provides access to the underlying [MPSolver], its variables,
 * constraints, and objective function after the optimization model is built.
 *
 * Example:
 * ```kotlin
 * val config: OrToolsConfiguration = ...
 * println("Objective value: ${config.objective.value()}")
 * ```
 */
public interface OrToolsConfiguration {

    /** The underlying OR-Tools solver instance. */
    public val solver: MPSolver

    /** The list of decision variables in the model. */
    public val variables: List<MPVariable>

    /** The list of constraints defined in the model. */
    public val constraints: List<MPConstraint>

    /** The model's objective function. */
    public val objective: MPObjective
}
