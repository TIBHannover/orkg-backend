package org.orkg.contenttypes.domain.actions.comparison

import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction.State

class ComparisonObservatoryValidator(
    observatoryRepository: ObservatoryRepository
) : ObservatoryValidator(observatoryRepository), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        validate(command.observatories)
        return state
    }
}
