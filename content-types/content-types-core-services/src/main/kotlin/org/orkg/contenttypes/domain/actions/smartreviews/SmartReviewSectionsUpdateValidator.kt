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
        command.sections?.also { sections ->
            val validationCache = mutableSetOf<ThingId>()
            state.smartReview!!.sections.forEach { section ->
                when (section) {
                    is SmartReviewComparisonSection -> section.comparison?.id?.also(validationCache::add)
                    is SmartReviewVisualizationSection -> section.visualization?.id?.also(validationCache::add)
                    is SmartReviewResourceSection -> section.resource?.id?.also(validationCache::add)
                    is SmartReviewPredicateSection -> section.predicate?.id?.also(validationCache::add)
                    is SmartReviewOntologySection -> {
                        section.entities.forEach { it.id?.also(validationCache::add) }
                        section.predicates.forEach { validationCache.add(it.id) }
                    }
                    is SmartReviewTextSection -> Unit
                }
            }
            sections.forEach { section ->
                abstractSmartReviewSectionValidator.validate(section, validationCache)
            }
        }
        return state
    }
}
