package io.github.bartlomiejkrawczyk.linearsolver.expression

import io.github.bartlomiejkrawczyk.linearsolver.utils.OptimizerStringUtils.formatDouble

/**
 * Represents a linear expression of variables, coefficients, and a constant term.
 *
 * Linear expressions are central to constraint and objective definitions
 * in linear and mixed-integer programming models.
 *
 * ### Example
 * ```kotlin
 * val x1 = numVar("x1")
 * val x2 = numVar("x2")
 *
 * val expr = 2 * x1 + 3 * x2 - 5 // -> 2x1 + 3x2 - 5
 * ```
 * @property coefficients Map from variable name to its coefficient.
 * @property constant The constant term of the expression.
 */
public data class LinearExpression(
    override val coefficients: Map<VariableName, Double> = emptyMap(),
    override val constant: Double = 0.0,
) : Expression {
    override fun toString(): String {
        return buildString {
            val terms = mutableListOf<String>()
            for ((varName, coeff) in coefficients) {
                val coeffStr = when {
                    coeff == 1.0 -> ""
                    coeff == -1.0 -> "-"
                    else -> "${formatDouble(coeff)} "
                }
                terms.add("$coeffStr$varName")
            }
            if (constant != 0.0 || terms.isEmpty()) {
                terms.add(formatDouble(constant))
            }
            append(terms.joinToString(" + ").replace("+ -", "- "))
        }
    }
}
