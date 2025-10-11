package io.github.bartlomiejkrawczyk.linearsolver.model

import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPObjective
import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.expression.Variable

class SolverConfiguration(

    val solver: MPSolver,

    val variables: List<Variable>,

    val constraints: List<MPConstraint>,

    val objective: MPObjective,
)
