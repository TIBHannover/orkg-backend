package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.CreateSmartReviewSectionAction.State
import org.orkg.contenttypes.input.SmartReviewSectionDefinition
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository

class SmartReviewSectionCreateValidator(
    private val abstractSmartReviewSectionValidator: AbstractSmartReviewSectionValidator
) : CreateSmartReviewSectionAction {
    constructor(
        resourceRepository: ResourceRepository,
        predicateRepository: PredicateRepository,
        thingRepository: ThingRepository,
    ) : this(AbstractSmartReviewSectionValidator(resourceRepository, predicateRepository, thingRepository))

    override fun invoke(command: CreateSmartReviewSectionCommand, state: State): State =
        state.also {
            abstractSmartReviewSectionValidator.validate(
                section = command as SmartReviewSectionDefinition,
                validIds = mutableSetOf()
            )
        }
}
