package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.ResearchFieldValidator
import eu.tib.orkg.prototype.contenttypes.services.actions.paper.PaperAction.*
import eu.tib.orkg.prototype.statements.spi.ResourceRepository

class PaperResearchFieldValidator(
    resourceRepository: ResourceRepository
) : ResearchFieldValidator(resourceRepository), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: State): State {
        validate(command.researchFields)
        return state
    }
}
