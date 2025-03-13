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
import org.orkg.contenttypes.input.AbstractSmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewTextSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewVisualizationSectionCommand
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewSectionUpdater(
    private val abstractSmartReviewSectionUpdater: AbstractSmartReviewSectionUpdater,
) : UpdateSmartReviewSectionAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractSmartReviewSectionUpdater(
            unsafeLiteralUseCases,
            unsafeResourceUseCases,
            statementService,
            unsafeStatementUseCases
        )
    )

    override fun invoke(command: UpdateSmartReviewSectionCommand, state: State): State {
        val section = state.smartReview!!.sections.single { it.id == command.smartReviewSectionId }
        if (!(command as AbstractSmartReviewSectionCommand).matchesSmartReviewSection(section)) {
            when (command) {
                is AbstractSmartReviewComparisonSectionCommand -> abstractSmartReviewSectionUpdater.updateComparisonSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewComparisonSection,
                    statements = state.statements
                )
                is AbstractSmartReviewVisualizationSectionCommand -> abstractSmartReviewSectionUpdater.updateVisualizationSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewVisualizationSection,
                    statements = state.statements
                )
                is AbstractSmartReviewResourceSectionCommand -> abstractSmartReviewSectionUpdater.updateResourceSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewResourceSection,
                    statements = state.statements
                )
                is AbstractSmartReviewPredicateSectionCommand -> abstractSmartReviewSectionUpdater.updatePredicateSection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewPredicateSection,
                    statements = state.statements
                )
                is AbstractSmartReviewOntologySectionCommand -> abstractSmartReviewSectionUpdater.updateOntologySection(
                    contributorId = command.contributorId,
                    newSection = command,
                    oldSection = section as SmartReviewOntologySection,
                    statements = state.statements
                )
                is AbstractSmartReviewTextSectionCommand -> abstractSmartReviewSectionUpdater.updateTextSection(
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
