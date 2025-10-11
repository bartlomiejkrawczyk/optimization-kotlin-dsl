package io.github.bartlomiejkrawczyk.linearsolver.model

import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPObjective
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPVariable

class SolverConfiguration(

    val solver: MPSolver,

    val variables: List<MPVariable>,

    val constraints: List<MPConstraint>,

    val objective: MPObjective,
)
