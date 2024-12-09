package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State

class ComparisonModifiableValidator : UpdateComparisonAction {
    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        if (state.comparison!!.published) {
            throw ComparisonNotModifiable(command.comparisonId)
        }
        return state
    }
}
