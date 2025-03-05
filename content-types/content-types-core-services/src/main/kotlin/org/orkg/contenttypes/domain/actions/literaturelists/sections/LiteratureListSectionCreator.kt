package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionCreator
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListSectionCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator,
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : CreateLiteratureListSectionAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        unsafeStatementUseCases,
        AbstractLiteratureListSectionCreator(unsafeStatementUseCases, unsafeResourceUseCases, unsafeLiteralUseCases),
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(
        command: CreateLiteratureListSectionCommand,
        state: CreateLiteratureListSectionState,
    ): CreateLiteratureListSectionState {
        val sectionId = abstractLiteratureListSectionCreator.create(
            contributorId = command.contributorId,
            section = command as LiteratureListSectionDefinition
        )
        if (command.index != null && command.index!! >= 0) {
            val sectionStatements = state.statements[command.literatureListId].orEmpty()
            statementCollectionPropertyUpdater.update(
                statements = sectionStatements,
                contributorId = command.contributorId,
                subjectId = command.literatureListId,
                predicateId = Predicates.hasSection,
                objects = sectionStatements.mapTo(mutableListOf()) { it.`object`.id }
                    .also { it.add(command.index!!.coerceAtMost(it.size), sectionId) }
            )
        } else {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = command.literatureListId,
                    predicateId = Predicates.hasSection,
                    objectId = sectionId
                )
            )
        }
        return state.copy(literatureListSectionId = sectionId)
    }
}
