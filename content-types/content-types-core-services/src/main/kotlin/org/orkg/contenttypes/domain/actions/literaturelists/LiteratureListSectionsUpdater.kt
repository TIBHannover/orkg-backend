package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListSectionsUpdater(
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator,
    private val abstractLiteratureListSectionDeleter: AbstractLiteratureListSectionDeleter,
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : UpdateLiteratureListAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        resourceService: ResourceUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractLiteratureListSectionCreator(unsafeStatementUseCases, unsafeResourceUseCases, unsafeLiteralUseCases),
        AbstractLiteratureListSectionDeleter(statementService, resourceService),
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(
        command: UpdateLiteratureListCommand,
        state: UpdateLiteratureListState,
    ): UpdateLiteratureListState {
        command.sections?.let { sections ->
            val oldSections = state.literatureList!!.sections.toMutableList()
            val new2old = sections.associateWith { newSection ->
                oldSections.firstOrNull { newSection.matchesLiteratureListSection(it) }?.also { oldSections.remove(it) }
            }
            val sectionIds = sections.map { newSection ->
                new2old[newSection]?.id ?: abstractLiteratureListSectionCreator.create(command.contributorId, newSection)
            }
            if (sectionIds != state.literatureList.sections.map { it.id }) {
                statementCollectionPropertyUpdater.update(
                    statements = state.statements[command.literatureListId].orEmpty(),
                    contributorId = command.contributorId,
                    subjectId = command.literatureListId,
                    predicateId = Predicates.hasSection,
                    objects = sectionIds
                )
                oldSections.forEach {
                    abstractLiteratureListSectionDeleter.delete(command.contributorId, command.literatureListId, it, state.statements)
                }
            }
        }
        return state
    }
}
