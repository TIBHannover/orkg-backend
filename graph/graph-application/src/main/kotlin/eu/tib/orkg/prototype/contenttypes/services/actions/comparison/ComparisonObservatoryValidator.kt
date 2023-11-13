package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.ObservatoryValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.State

class ComparisonObservatoryValidator(
    observatoryRepository: ObservatoryRepository
) : ObservatoryValidator(observatoryRepository), ComparisonAction {
    override operator fun invoke(command: CreateComparisonCommand, state: State): State {
        validate(command.observatories)
        return state
    }
}
