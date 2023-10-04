package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.OnlyOneResearchFieldAllowed
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.spi.ResourceRepository

class ResearchFieldValidator(
    private val resourceRepository: ResourceRepository
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        if (command.researchFields.size > 1) throw OnlyOneResearchFieldAllowed()
        command.researchFields.distinct().forEach { id ->
            resourceRepository.findById(id)
                .filter { Classes.researchField in it.classes }
                .orElseThrow { ResearchFieldNotFound(id) }
        }
        return state
    }
}
