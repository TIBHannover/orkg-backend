package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionState
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateContributionCommand
import eu.tib.orkg.prototype.statements.spi.ResourceRepository

class ContributionPaperExistenceValidator(
    private val resourceRepository: ResourceRepository
) : ContributionAction {
    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        resourceRepository.findPaperById(command.paperId)
            .orElseThrow { PaperNotFound(command.paperId) }
        return state
    }
}
