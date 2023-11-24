package org.orkg.contenttypes.domain.actions.contribution

import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.graph.output.ResourceRepository

class ContributionPaperExistenceValidator(
    private val resourceRepository: ResourceRepository
) : ContributionAction {
    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        resourceRepository.findPaperById(command.paperId)
            .orElseThrow { PaperNotFound(command.paperId) }
        return state
    }
}
