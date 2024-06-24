package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionCreator
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class LiteratureListSectionCreator(
    private val statementService: StatementUseCases,
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator,
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater
) : CreateLiteratureListSectionAction {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases
    ) : this(
        statementService,
        AbstractLiteratureListSectionCreator(statementService, resourceService, literalService),
        StatementCollectionPropertyUpdater(literalService, statementService)
    )

    override fun invoke(
        command: CreateLiteratureListSectionCommand,
        state: CreateLiteratureListSectionState
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
            statementService.add(
                userId = command.contributorId,
                subject = command.literatureListId,
                predicate = Predicates.hasSection,
                `object` = sectionId
            )
        }
        return state.copy(literatureListSectionId = sectionId)
    }
}
