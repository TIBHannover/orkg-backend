package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.AbstractAuthorListUpdater
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class PaperAuthorListUpdater(
    private val authorUpdater: AbstractAuthorListUpdater,
) : UpdatePaperAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
        listRepository: ListRepository,
    ) : this(
        AbstractAuthorListUpdater(
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            unsafeLiteralUseCases,
            listService,
            listRepository,
        ),
    )

    override fun invoke(command: UpdatePaperCommand, state: State): State {
        if (command.authors != null && command.authors != state.paper!!.authors) {
            authorUpdater.update(
                statements = state.statements,
                contributorId = command.contributorId,
                authors = state.authors,
                subjectId = command.paperId,
                extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            )
        }
        return state
    }
}
