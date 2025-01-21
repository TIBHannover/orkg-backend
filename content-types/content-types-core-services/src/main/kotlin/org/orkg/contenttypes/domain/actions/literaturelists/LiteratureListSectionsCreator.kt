package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.CreateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListState
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class LiteratureListSectionsCreator(
    private val statementService: StatementUseCases,
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator
) : CreateLiteratureListAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases
    ) : this(statementService, AbstractLiteratureListSectionCreator(statementService, unsafeResourceUseCases, literalService))

    override fun invoke(
        command: CreateLiteratureListCommand,
        state: CreateLiteratureListState
    ): CreateLiteratureListState {
        command.sections.forEach { section ->
            statementService.add(
                userId = command.contributorId,
                subject = state.literatureListId!!,
                predicate = Predicates.hasSection,
                `object` = abstractLiteratureListSectionCreator.create(command.contributorId, section)
            )
        }
        return state
    }
}
