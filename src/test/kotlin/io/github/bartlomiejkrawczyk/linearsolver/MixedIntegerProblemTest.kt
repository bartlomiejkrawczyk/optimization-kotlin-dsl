package io.github.bartlomiejkrawczyk.linearsolver

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.objective.Goal
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MixedIntegerProblemTest {

    @Test
    fun `optimization problem should be optimal`() {
        val solution = solve {
            solver(SolverType.SCIP_MIXED_INTEGER_PROGRAMMING)

            val x = intVar("x")
            val y = numVar("y")
            val z = boolVar("z")

            val variables = listOf(x, y, z)

            // OBJECTIVE
            x * 2 + y * 3 + 4 * z to Goal.MAX

            // or
            objective {
                x * 2 + y * 3 + 4 * z to Goal.MAX
            }

            // or
            max {
                x * 2 + y * 3 + 4 * z
            }

            // CONSTRAINTS
            x + y le 3
            y - 1 le 2

            5 * y eq (x + 3) * 2

            for (variable in variables) {
                variable le 1.5
            }

            constraint {
                variables.sum() le y
            }

            "Named constraint - y greater than x" {
                y ge x
            }

            "5y = 2(x + 3)" {
                5 * y eq 2 * (x + 3)
            }
        }

        val (status, config) = solution

        println("OBJECTIVE")
        println("Optimal objective value = ${config.objective.value()}")

        println("VARIABLES")
        config.variables.forEach { variable ->
            println("${variable.name()} = ${variable.solutionValue()}")
        }

        println(solution.exportModelAsLpFormat())

        Assertions.assertEquals(
            MPSolver.ResultStatus.OPTIMAL,
            status,
        )
    }
}
