package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.PublicationInfoCreator
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository

class PaperPublicationInfoCreator(
    resourceService: ResourceUseCases,
    resourceRepository: ResourceRepository,
    statementService: StatementUseCases,
    literalService: LiteralUseCases
) : PublicationInfoCreator(resourceService, resourceRepository, statementService, literalService), CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        if (command.publicationInfo != null) {
            create(command.contributorId, command.publicationInfo!!, state.paperId!!)
        }
        return state
    }
}
