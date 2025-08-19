package org.orkg.contenttypes.domain.actions.contributions

import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.contributions.ContributionAction.State
import org.orkg.graph.output.ResourceRepository

class ContributionPaperValidator(
    private val resourceRepository: ResourceRepository,
) : ContributionAction {
    override fun invoke(command: CreateContributionCommand, state: State): State {
        val paper = resourceRepository.findPaperById(command.paperId)
            .orElseThrow { PaperNotFound.withId(command.paperId) }
        if (!paper.modifiable) {
            throw PaperNotModifiable(command.paperId)
        }
        return state
    }
}
