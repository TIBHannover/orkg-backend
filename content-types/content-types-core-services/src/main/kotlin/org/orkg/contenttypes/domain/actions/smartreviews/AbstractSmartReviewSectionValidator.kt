package org.orkg.contenttypes.domain.actions.smartreviews

import dev.forkhandles.values.ofOrNull
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.InvalidSmartReviewTextSectionType
import org.orkg.contenttypes.domain.OntologyEntityNotFound
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.input.AbstractSmartReviewComparisonSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewOntologySectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewPredicateSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewResourceSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewTextSectionCommand
import org.orkg.contenttypes.input.AbstractSmartReviewVisualizationSectionCommand
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
    internal fun validate(section: AbstractSmartReviewSectionCommand, validIds: MutableSet<ThingId>) {
        Label.ofOrNull(section.heading) ?: throw InvalidLabel("heading")

        when (section) {
            is AbstractSmartReviewComparisonSectionCommand -> {
                validateWithCache(section.comparison, validIds) { comparisonId ->
                    resourceRepository.findById(comparisonId)
                        .filter { Classes.comparison in it.classes || Classes.comparisonPublished in it.classes }
                        .orElseThrow { ComparisonNotFound(comparisonId) }
                }
            }
            is AbstractSmartReviewVisualizationSectionCommand -> {
                validateWithCache(section.visualization, validIds) { visualizationId ->
                    resourceRepository.findById(visualizationId)
                        .filter { Classes.visualization in it.classes }
                        .orElseThrow { VisualizationNotFound(visualizationId) }
                }
            }
            is AbstractSmartReviewResourceSectionCommand -> {
                validateWithCache(section.resource, validIds) { resourceId ->
                    resourceRepository.findById(resourceId)
                        .orElseThrow { ResourceNotFound.withId(resourceId) }
                }
            }
            is AbstractSmartReviewPredicateSectionCommand -> {
                validateWithCache(section.predicate, validIds) { predicateId ->
                    predicateRepository.findById(predicateId)
                        .orElseThrow { PredicateNotFound(predicateId) }
                }
            }
            is AbstractSmartReviewOntologySectionCommand -> {
                val entitiesToValidate = section.entities.toSet() - validIds
                if (entitiesToValidate.isNotEmpty()) {
                    if (!thingRepository.existsAllById(entitiesToValidate)) {
                        throw OntologyEntityNotFound(entitiesToValidate)
                    }
                    validIds += section.entities
                }
                section.predicates.forEach { predicateId ->
                    validateWithCache(predicateId, validIds) {
                        predicateRepository.findById(predicateId)
                            .orElseThrow { PredicateNotFound(predicateId) }
                    }
                }
            }
            is AbstractSmartReviewTextSectionCommand -> {
                Description.ofOrNull(section.text) ?: throw InvalidDescription("text")
                if (section.`class` != null && section.`class` !in SmartReviewTextSection.types) {
                    throw InvalidSmartReviewTextSectionType(section.`class`!!)
                }
            }
        }
    }

    private inline fun <T> validateWithCache(element: T?, cache: MutableSet<T>, block: (T) -> Unit) {
        if (element != null && element !in cache) {
            block(element)
            cache += element
        }
    }
}
