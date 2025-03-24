package org.orkg.contenttypes.domain.actions.smartreviews

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateSmartReviewCommand
import org.orkg.contenttypes.domain.actions.smartreviews.CreateSmartReviewAction.State
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository

class SmartReviewSectionsCreateValidator(
    private val abstractSmartReviewSectionValidator: AbstractSmartReviewSectionValidator,
) : CreateSmartReviewAction {
    constructor(
        resourceRepository: ResourceRepository,
        predicateRepository: PredicateRepository,
        thingRepository: ThingRepository,
    ) : this(
        AbstractSmartReviewSectionValidator(resourceRepository, predicateRepository, thingRepository)
    )

    override fun invoke(command: CreateSmartReviewCommand, state: State): State {
        val validIds = mutableSetOf<ThingId>()
        command.sections.forEach { section ->
            abstractSmartReviewSectionValidator.validate(section, validIds)
        }
        return state
    }
}
