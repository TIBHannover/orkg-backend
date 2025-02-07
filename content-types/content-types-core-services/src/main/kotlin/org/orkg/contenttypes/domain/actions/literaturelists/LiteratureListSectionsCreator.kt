package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListSectionsCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator
) : CreateLiteratureListAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        unsafeStatementUseCases,
        AbstractLiteratureListSectionCreator(unsafeStatementUseCases, unsafeResourceUseCases, literalService)
    )

    override fun invoke(
        command: CreateLiteratureListCommand,
        state: CreateLiteratureListState
    ): CreateLiteratureListState {
        command.sections.forEach { section ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.literatureListId!!,
                    predicateId = Predicates.hasSection,
                    objectId = abstractLiteratureListSectionCreator.create(command.contributorId, section)
                )
            )
        }
        return state
    }
}
