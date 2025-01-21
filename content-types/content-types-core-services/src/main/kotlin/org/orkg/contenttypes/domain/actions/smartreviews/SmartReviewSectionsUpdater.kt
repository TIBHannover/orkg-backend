package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class SmartReviewSectionsUpdater(
    private val abstractSmartReviewSectionCreator: AbstractSmartReviewSectionCreator,
    private val abstractSmartReviewSectionDeleter: AbstractSmartReviewSectionDeleter,
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater
) : UpdateSmartReviewAction {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases
    ) : this(
        AbstractSmartReviewSectionCreator(statementService, unsafeResourceUseCases, literalService),
        AbstractSmartReviewSectionDeleter(statementService, resourceService),
        StatementCollectionPropertyUpdater(literalService, statementService)
    )

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        command.sections?.let { sections ->
            val oldSections = state.smartReview!!.sections.toMutableList()
            val new2old = sections.associateWith { newSection ->
                oldSections.firstOrNull { newSection.matchesSmartReviewSection(it) }?.also { oldSections.remove(it) }
            }
            val sectionIds = sections.map { newSection ->
                new2old[newSection]?.id ?: abstractSmartReviewSectionCreator.create(command.contributorId, newSection)
            }
            if (sectionIds != state.smartReview.sections.map { it.id }) {
                val contributionId = state.statements.findContributionId(command.smartReviewId)!!
                statementCollectionPropertyUpdater.update(
                    statements = state.statements[contributionId].orEmpty(),
                    contributorId = command.contributorId,
                    subjectId = contributionId,
                    predicateId = Predicates.hasSection,
                    objects = sectionIds
                )
                oldSections.forEach {
                    abstractSmartReviewSectionDeleter.delete(command.contributorId, contributionId, it, state.statements)
                }
            }
        }
        return state
    }
}
