package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.contenttypes.domain.LiteratureListListSection
import org.orkg.contenttypes.domain.LiteratureListTextSection
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionState
import org.orkg.contenttypes.domain.actions.literaturelists.AbstractLiteratureListSectionUpdater
import org.orkg.contenttypes.input.AbstractLiteratureListListSectionCommand
import org.orkg.contenttypes.input.AbstractLiteratureListSectionCommand
import org.orkg.contenttypes.input.AbstractLiteratureListTextSectionCommand
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class LiteratureListSectionUpdater(
    private val abstractLiteratureListSectionUpdater: AbstractLiteratureListSectionUpdater,
) : UpdateLiteratureListSectionAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        resourceService: ResourceUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractLiteratureListSectionUpdater(
            unsafeLiteralUseCases = unsafeLiteralUseCases,
            resourceService = resourceService,
            unsafeResourceUseCases = unsafeResourceUseCases,
            statementService = statementService,
            unsafeStatementUseCases = unsafeStatementUseCases
        )
    )

    override fun invoke(
        command: UpdateLiteratureListSectionCommand,
        state: UpdateLiteratureListSectionState,
    ): UpdateLiteratureListSectionState {
        val section = state.literatureList!!.sections.single { it.id == command.literatureListSectionId }
        if (!(command as AbstractLiteratureListSectionCommand).matchesLiteratureListSection(section)) {
            when (command) {
                is AbstractLiteratureListListSectionCommand -> abstractLiteratureListSectionUpdater.updateListSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as LiteratureListListSection,
                    statements = state.statements
                )
                is AbstractLiteratureListTextSectionCommand -> abstractLiteratureListSectionUpdater.updateTextSection(
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
