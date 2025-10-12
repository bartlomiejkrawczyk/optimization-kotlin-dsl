package io.github.bartlomiejkrawczyk.linearsolver.objective

import io.github.bartlomiejkrawczyk.linearsolver.OptimizerDslMarker
import io.github.bartlomiejkrawczyk.linearsolver.expression.Expression
import io.github.bartlomiejkrawczyk.linearsolver.model.OptimizerExtensions

@OptimizerDslMarker
class ObjectiveBuilder : OptimizerExtensions {

    infix fun Expression.to(goal: Goal): Objective {
        return Objective(
            expression = this@to,
            goal = goal,
        )
    }

    infix fun min(expression: Expression): Objective {
        return Objective(
            expression = expression,
            goal = Goal.MIN,
        )
    }

    infix fun max(expression: Expression): Objective {
        return Objective(
            expression = expression,
            goal = Goal.MAX,
        )
    }
}
