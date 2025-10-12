package io.github.bartlomiejkrawczyk.linearsolver.flownetwork

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.optimization
import io.github.bartlomiejkrawczyk.linearsolver.solver.SolverType
import io.github.bartlomiejkrawczyk.linearsolver.tensor.NamedTensor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FlowNetworkTest {

    val nodes = listOf(
        "s", "A", "B", "C", "D", "E", "F", "G", "H", "t"
    )
    val nodesWithout = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H"
    )
    val cost = mapOf(
        "A" to mapOf(
            "D" to 3,
            "E" to 6,
        ),
        "B" to mapOf(
            "D" to 6,
            "E" to 3,
        ),
        "C" to mapOf(
            "D" to 4,
            "E" to 5,
        ),
        "D" to mapOf(
            "E" to 2,
            "F" to 5,
            "G" to 7,
            "H" to 3,
        ),
        "E" to mapOf(
            "F" to 5,
            "G" to 4,
            "H" to 2,
        ),
    )
    val costTensor = NamedTensor(
        keys = listOf(nodes, nodes),
        values = cost,
        defaultValueProvider = { 0 },
    )
    val capacityMinCost = mapOf(
        "s" to mapOf(
            "A" to 10,
            "B" to 13,
            "C" to 22,
        ),
        "A" to mapOf(
            "D" to 8,
            "E" to 10,
        ),
        "B" to mapOf(
            "D" to 10,
            "E" to 13,
        ),
        "C" to mapOf(
            "D" to 10,
            "E" to 8,
        ),
        "D" to mapOf(
            "E" to 20,
            "F" to 16,
            "G" to 6,
            "H" to 10,
        ),
        "E" to mapOf(
            "F" to 7,
            "G" to 4,
            "H" to 2,
        ),
        "F" to mapOf(
            "t" to 15,
        ),
        "G" to mapOf(
            "t" to 10,
        ),
        "H" to mapOf(
            "t" to 10,
        ),
    )
    val capacityMinCostTensor = NamedTensor(
        keys = listOf(nodes, nodes),
        values = capacityMinCost,
        defaultValueProvider = { 0 },
    )
    val fGiven = 35

    @Test
    fun `optimize flow network - min cost`() {
        val (status, config) = optimization {
            solver(SolverType.GLOP_LINEAR_PROGRAMMING)

            val totalCost = numVar("totalCost", lowerBound = 0.0)
            val flows = tensorNumVar(
                tensorKeys = listOf(nodes, nodes),
                namePrefix = "flow",
                lowerBound = 0,
            )

            min {
                totalCost
            }

            "total cost constraint" {
                totalCost eq nodes.flatMap { f ->
                    nodes.map { t ->
                        costTensor[f, t] * flows[f, t]
                    }
                }.sum()
            }

            "initial flow constraint" {
                nodes.map { z -> flows["s", z] }.sum() eq fGiven
            }

            for (v in nodesWithout) {
                "Kirchhoff's law constraint - $v" {
                    nodes.map { z -> flows[v, z] }.sum() eq
                            nodes.map { u -> flows[u, v] }.sum()
                }
            }

            for (f in nodes) {
                for (t in nodes) {
                    "max flow constraint - $f $t" {
                        flows[f, t] le capacityMinCostTensor[f, t]
                    }
                }
            }
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

    val capacityMaxTotalFlow = mapOf(
        "s" to mapOf(
            "A" to 10,
            "B" to 13,
            "C" to 22,
        ),
        "A" to mapOf(
            "D" to 8,
            "E" to 10,
        ),
        "B" to mapOf(
            "D" to 10,
            "E" to 13,
        ),
        "C" to mapOf(
            "D" to 10,
            "E" to 8,
        ),
        "D" to mapOf(
            "E" to 20,
            "F" to 16,
            "G" to 6,
            "H" to 10,
        ),
        "E" to mapOf(
            "F" to 7,
            "G" to 4,
            "H" to 2,
        ),
        "F" to mapOf(
            "t" to 1000,
        ),
        "G" to mapOf(
            "t" to 1000,
        ),
        "H" to mapOf(
            "t" to 1000,
        ),
    )
    val capacityMaxTotalFlowTensor = NamedTensor(
        keys = listOf(nodes, nodes),
        values = capacityMaxTotalFlow,
        defaultValueProvider = { 0 },
    )

    @Test
    fun `optimize flow network - max total flow`() {
        val (status, config) = optimization {
            solver(SolverType.GLOP_LINEAR_PROGRAMMING)

            val totalFlow = numVar("totalFlow", lowerBound = 0.0)
            val flows = tensorNumVar(
                tensorKeys = listOf(nodes, nodes),
                namePrefix = "flow",
                lowerBound = 0,
            )

            max {
                totalFlow
            }

            "initial flow constraint" {
                nodes.map { z -> flows["s", z] }.sum() eq totalFlow
            }

            for (v in nodesWithout) {
                "Kirchhoff's law constraint - $v" {
                    nodes.map { z -> flows[v, z] }.sum() eq
                            nodes.map { u -> flows[u, v] }.sum()
                }
            }

            for (f in nodes) {
                for (t in nodes) {
                    "max flow constraint - $f $t" {
                        flows[f, t] le capacityMaxTotalFlowTensor[f, t]
                    }
                }
            }
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
