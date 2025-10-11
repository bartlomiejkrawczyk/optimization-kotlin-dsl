package io.github.bartlomiejkrawczyk.linearsolver

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.objective.Goal
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MixedIntegerProblemTest {

    @Test
    fun `optimization problem should be optimal`() {
        val (status, config) = optimization {
            solver(SolverType.SCIP_MIXED_INTEGER_PROGRAMMING)

            val x = intVar("x")
            val y = numVar("y")
            val z = boolVar("z")

            val variables = listOf(x, y, z)

            x le 3
            y le 2
            5 * y eq (x + 3) * 2

            for (variable in variables) {
                variable le 1
            }

            x * 2 + y * 3 + 4 * z to Goal.MAX
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
