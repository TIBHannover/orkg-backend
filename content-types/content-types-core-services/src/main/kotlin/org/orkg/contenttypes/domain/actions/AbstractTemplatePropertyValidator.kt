package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidDatatype
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.input.LiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.NumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.ResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.StringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.TemplatePropertyCommand
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class AbstractTemplatePropertyValidator(
    private val predicateRepository: PredicateRepository,
    private val classRepository: ClassRepository,
) {
    internal fun validate(property: TemplatePropertyCommand) {
        Label.ofOrNull(property.label) ?: throw InvalidLabel()
        property.placeholder?.also { Label.ofOrNull(it) ?: throw InvalidLabel("placeholder") }
        property.description?.also { Description.ofOrNull(it) ?: throw InvalidDescription("description") }
        property.minCount?.also { min ->
            if (min < 0) {
                throw InvalidMinCount(min)
            }
        }
        property.maxCount?.also { max ->
            if (max < 0) {
                throw InvalidMaxCount(max)
            }
            property.minCount?.also { min ->
                if (max in 1 until min) {
                    throw InvalidCardinality(min, max)
                }
            }
        }
        if (property is StringLiteralTemplatePropertyCommand) {
            if (Literals.XSD.fromClass(property.datatype) != Literals.XSD.STRING) {
                throw InvalidDatatype(property.datatype, Literals.XSD.STRING.`class`)
            }
            property.pattern?.also { pattern ->
                try {
                    Regex(pattern)
                } catch (e: Exception) {
                    throw InvalidRegexPattern(pattern, e)
                }
            }
        } else if (property is NumberLiteralTemplatePropertyCommand) {
            val xsd = Literals.XSD.fromClass(property.datatype)
            if (xsd?.isNumber != true) {
                throw InvalidDatatype(property.datatype, *Literals.XSD.entries.filter { it.isNumber }.map { it.`class` }.toTypedArray())
            }
            val minInclusive = property.minInclusive
            val maxInclusive = property.maxInclusive
            if (minInclusive != null && maxInclusive != null && minInclusive.toDouble() > maxInclusive.toDouble()) {
                throw InvalidBounds(minInclusive, maxInclusive)
            }
        }
        predicateRepository.findById(property.path).orElseThrow { PredicateNotFound(property.path) }
        val range = when (property) {
            is LiteralTemplatePropertyCommand -> property.datatype
            is ResourceTemplatePropertyCommand -> property.`class`
            else -> null
        }
        if (range != null) {
            classRepository.findById(range).orElseThrow { ClassNotFound.withThingId(range) }
        }
    }
}
