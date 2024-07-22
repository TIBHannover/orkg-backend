package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.LiteratureListListSection
import org.orkg.contenttypes.domain.LiteratureListTextSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionUpdater
import org.orkg.contenttypes.input.LiteratureListListSectionDefinition
import org.orkg.contenttypes.input.LiteratureListSectionDefinition
import org.orkg.contenttypes.input.LiteratureListTextSectionDefinition
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
        if (!(command as LiteratureListSectionDefinition).matchesLiteratureListSection(section)) {
            when (command) {
                is LiteratureListListSectionDefinition -> abstractLiteratureListSectionUpdater.updateListSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as LiteratureListListSection,
                    statements = state.statements
                )
                is LiteratureListTextSectionDefinition -> abstractLiteratureListSectionUpdater.updateTextSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as LiteratureListTextSection,
                    statements = state.statements
                )
            }
        }
        return state
    }
}
