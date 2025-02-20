package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.sections.UpdateSmartReviewSectionAction.State
import org.orkg.contenttypes.input.SmartReviewComparisonSectionDefinition
import org.orkg.contenttypes.input.SmartReviewOntologySectionDefinition
import org.orkg.contenttypes.input.SmartReviewPredicateSectionDefinition
import org.orkg.contenttypes.input.SmartReviewResourceSectionDefinition
import org.orkg.contenttypes.input.SmartReviewSectionDefinition
import org.orkg.contenttypes.input.SmartReviewTextSectionDefinition
import org.orkg.contenttypes.input.SmartReviewVisualizationSectionDefinition
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewSectionUpdater(
    private val abstractSmartReviewSectionUpdater: AbstractSmartReviewSectionUpdater,
) : UpdateSmartReviewSectionAction {
    constructor(
        literalService: LiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractSmartReviewSectionUpdater(
            literalService,
            unsafeResourceUseCases,
            statementService,
            unsafeStatementUseCases
        )
    )

    override fun invoke(command: UpdateSmartReviewSectionCommand, state: State): State {
        val section = state.smartReview!!.sections.single { it.id == command.smartReviewSectionId }
        if (!(command as SmartReviewSectionDefinition).matchesSmartReviewSection(section)) {
            when (command) {
                is SmartReviewComparisonSectionDefinition -> abstractSmartReviewSectionUpdater.updateComparisonSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewComparisonSection,
                    statements = state.statements
                )
                is SmartReviewVisualizationSectionDefinition -> abstractSmartReviewSectionUpdater.updateVisualizationSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewVisualizationSection,
                    statements = state.statements
                )
                is SmartReviewResourceSectionDefinition -> abstractSmartReviewSectionUpdater.updateResourceSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewResourceSection,
                    statements = state.statements
                )
                is SmartReviewPredicateSectionDefinition -> abstractSmartReviewSectionUpdater.updatePredicateSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewPredicateSection,
                    statements = state.statements
                )
                is SmartReviewOntologySectionDefinition -> abstractSmartReviewSectionUpdater.updateOntologySection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewOntologySection,
                    statements = state.statements
                )
                is SmartReviewTextSectionDefinition -> abstractSmartReviewSectionUpdater.updateTextSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewTextSection,
                    statements = state.statements
                )
            }
        }
        return state
    }
}
