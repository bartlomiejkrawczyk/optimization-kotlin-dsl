package io.github.bartlomiejkrawczyk.linearsolver

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.objective.Goal
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IntegerProgrammingTest {

    @Test
    fun `integer programming optimization should be optimal`() {
        // https://github.com/google/or-tools/blob/stable/examples/java/IntegerProgramming.java

        val (status, config) = optimization {
            solver(SolverType.SCIP_MIXED_INTEGER_PROGRAMMING)

            val x1 = intVar("x1", lowerBound = 0.0)
            val x2 = intVar("x2", lowerBound = 0.0)

            // OBJECTIVE
            x1 + 2 * x2 to Goal.MIN

            // CONSTRAINTS
            2 * x2 + 3 * x1 ge 17

            println("Number of variables = ${this.variables.size}")
            println("Number of constraints = ${this.constraints.size}")
        }

        println("OBJECTIVE")
        println("Optimal objective value = ${config.objective.value()}")

        println("VARIABLES")
        config.variables.forEach { variable ->
            println("${variable.name()} = ${variable.solutionValue()}")
        }

        println("CONSTRAINTS")
        config.constraints.forEach { constraint ->
            println("${constraint.name()} = ${constraint.dualValue()}")
        }

        Assertions.assertEquals(
            MPSolver.ResultStatus.OPTIMAL,
            status,
        )
    }
}
