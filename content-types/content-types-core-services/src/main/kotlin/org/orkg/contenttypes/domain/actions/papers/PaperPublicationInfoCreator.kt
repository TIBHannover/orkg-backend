package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.PublicationInfoCreator
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository

class PaperPublicationInfoCreator(
    private val publicationInfoCreator: PublicationInfoCreator,
) : CreatePaperAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        resourceRepository: ResourceRepository,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        PublicationInfoCreator(unsafeResourceUseCases, resourceRepository, unsafeStatementUseCases, unsafeLiteralUseCases)
    )

    override fun invoke(command: CreatePaperCommand, state: State): State {
        if (command.publicationInfo != null) {
            publicationInfoCreator.create(
                contributorId = command.contributorId,
                publicationInfo = command.publicationInfo!!,
                subjectId = state.paperId!!
            )
        }
        return state
    }
}
