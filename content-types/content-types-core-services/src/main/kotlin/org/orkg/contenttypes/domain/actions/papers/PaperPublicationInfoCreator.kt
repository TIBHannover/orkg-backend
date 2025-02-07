package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.PublicationInfoCreator
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository

class PaperPublicationInfoCreator(
    unsafeResourceUseCases: UnsafeResourceUseCases,
    resourceRepository: ResourceRepository,
    unsafeStatementUseCases: UnsafeStatementUseCases,
    literalService: LiteralUseCases
) : PublicationInfoCreator(unsafeResourceUseCases, resourceRepository, unsafeStatementUseCases, literalService), CreatePaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        if (command.publicationInfo != null) {
            create(command.contributorId, command.publicationInfo!!, state.paperId!!)
        }
        return state
    }
}
