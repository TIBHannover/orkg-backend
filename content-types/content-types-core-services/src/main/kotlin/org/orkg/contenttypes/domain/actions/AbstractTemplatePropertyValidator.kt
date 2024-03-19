package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.input.LiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.ResourceTemplatePropertyDefinition
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

abstract class AbstractTemplatePropertyValidator(
    private val predicateRepository: PredicateRepository,
    private val classRepository: ClassRepository
) {
    internal fun validate(property: TemplatePropertyDefinition) {
        Label.ofOrNull(property.label) ?: throw InvalidLabel()
        property.placeholder?.also { Label.ofOrNull(it) ?: throw InvalidLabel() }
        property.description?.also { Label.ofOrNull(it) ?: throw InvalidLabel() }
        property.minCount?.let { min ->
            if (min < 0) {
                throw InvalidMinCount(min)
            }
        }
        property.maxCount?.let { max ->
            if (max < 0) {
                throw InvalidMaxCount(max)
            }
            property.minCount?.let { min ->
                if (max < min) {
                    throw InvalidCardinality(min, max)
                }
            }
        }
        property.pattern?.let { pattern ->
            try {
                Regex(pattern)
            } catch (e: Exception) {
                throw InvalidRegexPattern(pattern, e)
            }
        }
        predicateRepository.findById(property.path).orElseThrow { PredicateNotFound(property.path) }
        val range = when (property) {
            is LiteralTemplatePropertyDefinition -> property.datatype
            is ResourceTemplatePropertyDefinition -> property.`class`
        }
        classRepository.findById(range).orElseThrow { ClassNotFound.withThingId(range) }
    }
}
