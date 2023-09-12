package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.application.ObservatoryNotFound
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.OnlyOneObservatoryAllowed

class ObservatoryValidator(
    private val observatoryRepository: ObservatoryRepository
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        if (command.observatories.size > 1) throw OnlyOneObservatoryAllowed()
        command.observatories.distinct().forEach {
            observatoryRepository.findById(it).orElseThrow { ObservatoryNotFound(it) }
        }
        return state
    }
}
