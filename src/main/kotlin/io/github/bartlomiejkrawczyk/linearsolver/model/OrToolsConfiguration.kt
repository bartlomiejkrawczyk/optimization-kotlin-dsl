package io.github.bartlomiejkrawczyk.linearsolver.model

import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPObjective
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPVariable

public interface OrToolsConfiguration {

    public val solver: MPSolver

    public val variables: List<MPVariable>

    public val constraints: List<MPConstraint>

    public val objective: MPObjective
}
