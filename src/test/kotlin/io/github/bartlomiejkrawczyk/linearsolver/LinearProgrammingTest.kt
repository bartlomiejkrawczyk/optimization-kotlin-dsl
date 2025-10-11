package io.github.bartlomiejkrawczyk.linearsolver

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.objective.Goal
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LinearProgrammingTest {

    @Test
    fun `linear programming optimization should be optimal`() {
        // https://github.com/google/or-tools/blob/stable/examples/java/LinearProgramming.java

        val (status, config) = optimization {
            solver(SolverType.GLOP_LINEAR_PROGRAMMING)

            val x1 = numVar("x1", lowerBound = 0.0)
            val x2 = numVar("x2", lowerBound = 0.0)
            val x3 = numVar("x3", lowerBound = 0.0)

            // OBJECTIVE
            10 * x1 + 6 * x2 + 4 * x3 to Goal.MAX

            // CONSTRAINTS
            x1 + x2 + x3 le 100
            10 * x1 + 4 * x2 + 5 * x3 le 600
            2 * x1 + 2 * x2 + 6 * x3 le 300

            println("Number of variables = ${this.variables.size}")
            println("Number of constraints = ${this.constraints.size}")
        }

        println("OBJECTIVE")
        println("Optimal objective value = ${config.objective.value()}")

        println("VARIABLES")
        config.variables.forEach { variable ->
            println("${variable.name()} = ${variable.solutionValue()}")
        }

        Assertions.assertEquals(
            MPSolver.ResultStatus.OPTIMAL,
            status,
        )
    }
}
