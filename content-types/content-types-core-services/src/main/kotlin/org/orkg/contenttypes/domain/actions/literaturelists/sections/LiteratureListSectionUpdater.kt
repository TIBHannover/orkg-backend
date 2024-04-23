package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.ListSection
import org.orkg.contenttypes.domain.TextSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionUpdater
import org.orkg.contenttypes.input.ListSectionDefinition
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.TextSectionDefinition
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class LiteratureListSectionUpdater(
    private val abstractLiteratureListSectionUpdater: AbstractLiteratureListSectionUpdater
) : UpdateLiteratureListSectionAction {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
    ) : this(AbstractLiteratureListSectionUpdater(literalService, resourceService, statementService))

    override fun invoke(
        command: UpdateLiteratureListSectionCommand,
        state: UpdateLiteratureListSectionState
    ): UpdateLiteratureListSectionState {
        val section = state.literatureList!!.sections.single { it.id == command.literatureListSectionId }
        if (!(command as LiteratureListSectionDefinition).matchesListSection(section)) {
            when (command) {
                is ListSectionDefinition -> abstractLiteratureListSectionUpdater.updateListSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as ListSection,
                    statements = state.statements
                )
                is TextSectionDefinition -> abstractLiteratureListSectionUpdater.updateTextSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as TextSection,
                    statements = state.statements
                )
            }
        }
        return state
    }
}
