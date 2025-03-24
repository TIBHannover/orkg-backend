package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.UpdateSmartReviewAction.State
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository

class SmartReviewSectionsUpdateValidator(
    private val abstractSmartReviewSectionValidator: AbstractSmartReviewSectionValidator,
) : UpdateSmartReviewAction {
    constructor(
        resourceRepository: ResourceRepository,
        predicateRepository: PredicateRepository,
        thingRepository: ThingRepository,
    ) : this(
        AbstractSmartReviewSectionValidator(resourceRepository, predicateRepository, thingRepository)
    )

    override fun invoke(command: UpdateSmartReviewCommand, state: State): State {
        command.sections?.let { sections ->
            val validIds = mutableSetOf<ThingId>()
            state.smartReview!!.sections.forEach { section ->
                when (section) {
                    is SmartReviewComparisonSection -> section.comparison?.id?.let(validIds::add)
                    is SmartReviewVisualizationSection -> section.visualization?.id?.let(validIds::add)
                    is SmartReviewResourceSection -> section.resource?.id?.let(validIds::add)
                    is SmartReviewPredicateSection -> section.predicate?.id?.let(validIds::add)
                    is SmartReviewOntologySection -> {
                        section.entities.forEach { it.id?.let(validIds::add) }
                        section.predicates.forEach { validIds.add(it.id) }
                    }
                    is SmartReviewTextSection -> Unit
                }
            }
            sections.forEach { section ->
                abstractSmartReviewSectionValidator.validate(section, validIds)
            }
        }
        return state
    }
}
