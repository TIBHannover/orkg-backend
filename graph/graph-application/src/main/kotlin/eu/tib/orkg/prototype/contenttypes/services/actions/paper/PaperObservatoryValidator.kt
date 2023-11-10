package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.ObservatoryValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.paper.PaperAction.*

class PaperObservatoryValidator(
    observatoryRepository: ObservatoryRepository
) : ObservatoryValidator(observatoryRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: State): State {
        validate(command.observatories)
        return state
    }
}
