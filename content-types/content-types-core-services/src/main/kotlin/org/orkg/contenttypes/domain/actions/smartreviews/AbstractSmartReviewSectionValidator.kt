package org.orkg.contenttypes.domain.actions.smartreviews

import dev.forkhandles.values.ofOrNull
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.InvalidSmartReviewTextSectionType
import org.orkg.contenttypes.domain.OntologyEntityNotFound
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.input.SmartReviewComparisonSectionDefinition
import org.orkg.contenttypes.input.SmartReviewOntologySectionDefinition
import org.orkg.contenttypes.input.SmartReviewPredicateSectionDefinition
import org.orkg.contenttypes.input.SmartReviewResourceSectionDefinition
import org.orkg.contenttypes.input.SmartReviewSectionDefinition
import org.orkg.contenttypes.input.SmartReviewTextSectionDefinition
import org.orkg.contenttypes.input.SmartReviewVisualizationSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository

class AbstractSmartReviewSectionValidator(
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository,
    private val thingRepository: ThingRepository,
) {
    internal fun validate(section: SmartReviewSectionDefinition, validIds: MutableSet<ThingId>) {
        Label.ofOrNull(section.heading) ?: throw InvalidLabel("heading")

        when (section) {
            is SmartReviewComparisonSectionDefinition -> {
                validateWitchCache(section.comparison, validIds) { comparisonId ->
                    resourceRepository.findById(comparisonId)
                        .filter { Classes.comparison in it.classes }
                        .orElseThrow { ComparisonNotFound(comparisonId) }
                }
            }
            is SmartReviewVisualizationSectionDefinition -> {
                validateWitchCache(section.visualization, validIds) { visualizationId ->
                    resourceRepository.findById(visualizationId)
                        .filter { Classes.visualization in it.classes }
                        .orElseThrow { VisualizationNotFound(visualizationId) }
                }
            }
            is SmartReviewResourceSectionDefinition -> {
                validateWitchCache(section.resource, validIds) { resourceId ->
                    resourceRepository.findById(resourceId)
                        .orElseThrow { ResourceNotFound.withId(resourceId) }
                }
            }
            is SmartReviewPredicateSectionDefinition -> {
                validateWitchCache(section.predicate, validIds) { predicateId ->
                    predicateRepository.findById(predicateId)
                        .orElseThrow { PredicateNotFound(predicateId) }
                }
            }
            is SmartReviewOntologySectionDefinition -> {
                val entitiesToValidate = section.entities.toSet() - validIds
                if (entitiesToValidate.isNotEmpty()) {
                    if (!thingRepository.existsAll(entitiesToValidate)) {
                        throw OntologyEntityNotFound(entitiesToValidate)
                    }
                    validIds += section.entities
                }
                section.predicates.forEach { predicateId ->
                    validateWitchCache(predicateId, validIds) {
                        predicateRepository.findById(predicateId)
                            .orElseThrow { PredicateNotFound(predicateId) }
                    }
                }
            }
            is SmartReviewTextSectionDefinition -> {
                Description.ofOrNull(section.text) ?: throw InvalidDescription("text")
                if (section.`class` !in SmartReviewTextSection.types) {
                    throw InvalidSmartReviewTextSectionType(section.`class`)
                }
            }
        }
    }

    private inline fun <T> validateWitchCache(element: T?, cache: MutableSet<T>, block: (T) -> Unit) {
        if (element != null && element !in cache) {
            block(element)
            cache += element
        }
    }
}
