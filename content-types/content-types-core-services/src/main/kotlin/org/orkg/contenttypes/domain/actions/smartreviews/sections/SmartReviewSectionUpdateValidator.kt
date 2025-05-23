package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.common.ThingId
import org.orkg.common.mutableSetOfNotNull
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSectionTypeMismatch
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.orkg.contenttypes.domain.UnrelatedSmartReviewSection
import org.orkg.contenttypes.domain.actions.UpdateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionValidator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.UpdateSmartReviewSectionAction.State
import org.orkg.contenttypes.input.UpdateSmartReviewSectionUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository

class SmartReviewSectionUpdateValidator(
    private val abstractSmartReviewSectionValidator: AbstractSmartReviewSectionValidator,
) : UpdateSmartReviewSectionAction {
    constructor(
        resourceRepository: ResourceRepository,
        predicateRepository: PredicateRepository,
        thingRepository: ThingRepository,
    ) : this(
        AbstractSmartReviewSectionValidator(resourceRepository, predicateRepository, thingRepository)
    )

    override fun invoke(command: UpdateSmartReviewSectionCommand, state: State): State {
        val section = state.smartReview!!.sections.singleOrNull { it.id == command.smartReviewSectionId }
            ?: throw UnrelatedSmartReviewSection(command.smartReviewId, command.smartReviewSectionId)
        when (command) {
            is UpdateSmartReviewSectionUseCase.UpdateComparisonSectionCommand -> {
                if (section !is SmartReviewComparisonSection) {
                    throw SmartReviewSectionTypeMismatch.mustBeComparisonSection()
                }
                val validationCache = mutableSetOfNotNull(section.comparison?.id)
                abstractSmartReviewSectionValidator.validate(command, validationCache)
            }
            is UpdateSmartReviewSectionUseCase.UpdateVisualizationSectionCommand -> {
                if (section !is SmartReviewVisualizationSection) {
                    throw SmartReviewSectionTypeMismatch.mustBeVisualizationSection()
                }
                val validationCache = mutableSetOfNotNull(section.visualization?.id)
                abstractSmartReviewSectionValidator.validate(command, validationCache)
            }
            is UpdateSmartReviewSectionUseCase.UpdateResourceSectionCommand -> {
                if (section !is SmartReviewResourceSection) {
                    throw SmartReviewSectionTypeMismatch.mustBeResourceSection()
                }
                val validationCache = mutableSetOfNotNull(section.resource?.id)
                abstractSmartReviewSectionValidator.validate(command, validationCache)
            }
            is UpdateSmartReviewSectionUseCase.UpdatePredicateSectionCommand -> {
                if (section !is SmartReviewPredicateSection) {
                    throw SmartReviewSectionTypeMismatch.mustBePredicateSection()
                }
                val validationCache = mutableSetOfNotNull(section.predicate?.id)
                abstractSmartReviewSectionValidator.validate(command, validationCache)
            }
            is UpdateSmartReviewSectionUseCase.UpdateOntologySectionCommand -> {
                if (section !is SmartReviewOntologySection) {
                    throw SmartReviewSectionTypeMismatch.mustBeOntologySection()
                }
                val validationCache = mutableSetOf<ThingId>()
                section.entities.forEach { entity ->
                    entity.id?.also { id -> validationCache.add(id) }
                }
                section.predicates.forEach {
                    validationCache.add(it.id)
                }
                abstractSmartReviewSectionValidator.validate(command, validationCache)
            }
            is UpdateSmartReviewSectionUseCase.UpdateTextSectionCommand -> {
                if (section !is SmartReviewTextSection) {
                    throw SmartReviewSectionTypeMismatch.mustBeTextSection()
                }
                abstractSmartReviewSectionValidator.validate(command, mutableSetOf())
            }
        }
        return state
    }
}
