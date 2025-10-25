package io.github.bartlomiejkrawczyk.linearsolver.objective

import com.google.ortools.linearsolver.MPSolver
import io.github.bartlomiejkrawczyk.linearsolver.expression.Variable
import io.github.bartlomiejkrawczyk.linearsolver.expression.VariableName
import io.github.bartlomiejkrawczyk.linearsolver.model.OrToolsConfiguration
import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfiguration
import io.github.bartlomiejkrawczyk.linearsolver.model.SolverConfigurationBuilder

/**
 * Represents the result of solving an optimization problem.
 *
 * Provides access to solver status, configuration, and utility methods
 * for inspecting or exporting the solution.
 *
 * Example:
 * ```kotlin
 * val solution = optimization {
 *     val x1 = numVar("x1")
 *     val x2 = numVar("x2")
 *     constraint { x1 + x2 le 10 }
 *     max(x1 + 2*x2)
 * }
 *
 * println("Status: ${solution.status}")
 * println("Objective: ${solution.objectiveValue}")
 * println(solution.exportModelAsLpFormat())
 * ```
 */
public data class Solution(

    /** The solver status (e.g., OPTIMAL, FEASIBLE, INFEASIBLE). */
    public val status: MPSolver.ResultStatus,

    /** The final solver configuration, including solver, variables, and constraints. */
    public val config: SolverConfiguration,

    /** The builder used to create the model. */
    public val builder: SolverConfigurationBuilder,
) : OrToolsConfiguration by config {

    /** Returns the final value of the objective function. */
    val objectiveValue: Double
        get() = config.objective.value()

    /**
     * Retrieves a variable by its name.
     */
    public operator fun get(variableName: String): Variable {
        val variableName = VariableName(variableName)
        return builder.variables[variableName]
            ?: throw IllegalArgumentException("Unknown variable $variableName")
    }

    /**
     * Exports the model in LP text format.
     *
     * Example:
     * ```kotlin
     * println(solution.exportModelAsLpFormat())
     * // -> LP model as string representation
     * ```
     */
    public fun exportModelAsLpFormat(): String =
        config.solver.exportModelAsLpFormat()

    val optimal: Boolean
        get() = status == MPSolver.ResultStatus.OPTIMAL

    override fun toString(): String {
        return buildString {
            appendLine("Solution:")
            appendLine("  Status: $status")
            appendLine("  Objective:")
            appendLine("    ${builder.objective}")
            appendLine("    value = $objectiveValue")
            appendLine("  Variables:")
            for (variable in config.variables) {
                appendLine("    ${variable.name()} = ${variable.solutionValue()}")
            }
            appendLine("  Constraints:")
            for (constraint in builder.constraints) {
                appendLine("    $constraint")
                appendLine("      dualValue = ${constraint.dualValue}")
                appendLine("      basisStatus = ${constraint.basisStatus}")
            }
        }
    }

    public fun print() {
        println(this.toString())
    }
}
