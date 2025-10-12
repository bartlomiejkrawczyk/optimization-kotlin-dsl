package io.github.bartlomiejkrawczyk.linearsolver.solver

/**
 * Supported solver backends for optimization.
 *
 * Some solvers are specialized for linear programming, while others
 * handle mixed-integer or SAT problems.
 *
 * Example:
 * ```kotlin
 * val solverType = SolverType.SCIP_MIXED_INTEGER_PROGRAMMING
 * val config = SolverConfigurationBuilder().apply { solver(solverType) }.build()
 * ```
 */
public enum class SolverType {
    CLP_LINEAR_PROGRAMMING, CLP,
    CBC_MIXED_INTEGER_PROGRAMMING, CBC,

    /**
     * Recommended for linear programming.
     */
    GLOP_LINEAR_PROGRAMMING, GLOP,

    BOP_INTEGER_PROGRAMMING, BOP,
    SAT_INTEGER_PROGRAMMING, SAT, CP_SAT,

    /**
     * Recommended for mixed integer programming.
     */
    SCIP_MIXED_INTEGER_PROGRAMMING, SCIP,

    GUROBI_LINEAR_PROGRAMMING, GUROBI_LP,
    GUROBI_MIXED_INTEGER_PROGRAMMING, GUROBI, GUROBI_MIP,

    CPLEX_LINEAR_PROGRAMMING, CPLEX_LP,
    CPLEX_MIXED_INTEGER_PROGRAMMING, CPLEX, CPLEX_MIP,

    XPRESS_LINEAR_PROGRAMMING, XPRESS_LP,
    XPRESS_MIXED_INTEGER_PROGRAMMING, XPRESS, XPRESS_MIP,

    GLPK_LINEAR_PROGRAMMING, GLPK_LP,
    GLPK_MIXED_INTEGER_PROGRAMMING, GLPK, GLPK_MIP,
}
