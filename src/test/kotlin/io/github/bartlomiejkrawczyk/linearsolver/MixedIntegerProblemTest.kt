package io.github.bartlomiejkrawczyk.linearsolver

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.objective.Goal
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MixedIntegerProblemTest {

    @Test
    fun `optimization problem should be optimal`() {
        val status = optimization {
            solver(SolverType.CBC_MIXED_INTEGER_PROGRAMMING)

            val x = intVar("x")
            val y = numVar("y")
            val z = boolVar("z")

            val variables = listOf(x, y, z)

            x le 3
            y le 2
            5 * y le (x + 3) * 2

            for (variable in variables) {
                variable le 1
            }

            x * 2 + y * 3 + 4 * z to Goal.MIN
        }

        Assertions.assertEquals(
            MPSolver.ResultStatus.OPTIMAL,
            status,
        )
    }
}
