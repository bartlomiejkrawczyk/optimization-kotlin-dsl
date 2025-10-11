package io.github.bartlomiejkrawczyk

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.objective.Goal
import io.github.bartlomiejkrawczyk.solver.SolverType
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

            x lessEquals 3
            y lessEquals 2
            5 * y lessEquals (x + 3) * 2

            for (variable in variables) {
                variable greaterEquals 1
            }

            x * 2 + y * 3 + 4 * z to Goal.MIN
        }

        Assertions.assertEquals(
            MPSolver.ResultStatus.OPTIMAL,
            status,
        )
    }
}
