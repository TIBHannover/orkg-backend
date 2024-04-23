package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionCreator
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class LiteratureListSectionCreator(
    private val statementService: StatementUseCases,
    private val abstractLiteratureListSectionCreator: AbstractLiteratureListSectionCreator
) : CreateLiteratureListSectionAction {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases
    ) : this(statementService, AbstractLiteratureListSectionCreator(statementService, resourceService, literalService))

    override fun invoke(
        command: CreateLiteratureListSectionCommand,
        state: CreateLiteratureListSectionState
    ): CreateLiteratureListSectionState {
        val sectionId = abstractLiteratureListSectionCreator.create(
            contributorId = command.contributorId,
            section = command as LiteratureListSectionDefinition
        )
        statementService.add(
            userId = command.contributorId,
            subject = command.literatureListId,
            predicate = Predicates.hasSection,
            `object` = sectionId
        )
        return state.copy(literatureListSectionId = sectionId)
    }
}
