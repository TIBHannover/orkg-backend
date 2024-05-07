package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidCardinality
import org.orkg.contenttypes.domain.InvalidDatatype
import org.orkg.contenttypes.domain.InvalidMaxCount
import org.orkg.contenttypes.domain.InvalidMinCount
import org.orkg.contenttypes.domain.InvalidRegexPattern
import org.orkg.contenttypes.input.LiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.NumberLiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.ResourceTemplatePropertyDefinition
import org.orkg.contenttypes.input.StringLiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class AbstractTemplatePropertyValidator(
    private val predicateRepository: PredicateRepository,
    private val classRepository: ClassRepository
) {
    internal fun validate(property: TemplatePropertyDefinition) {
        Label.ofOrNull(property.label) ?: throw InvalidLabel()
        property.placeholder?.also { Label.ofOrNull(it) ?: throw InvalidLabel("placeholder") }
        property.description?.also { Label.ofOrNull(it) ?: throw InvalidLabel("description") }
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
                if (max in 1 until min) {
                    throw InvalidCardinality(min, max)
                }
            }
        }
        if (property is StringLiteralTemplatePropertyDefinition) {
            if (Literals.XSD.fromClass(property.datatype) != Literals.XSD.STRING) {
                throw InvalidDatatype(property.datatype, Literals.XSD.STRING.`class`)
            }
            property.pattern?.let { pattern ->
                try {
                    Regex(pattern)
                } catch (e: Exception) {
                    throw InvalidRegexPattern(pattern, e)
                }
            }
        } else if (property is NumberLiteralTemplatePropertyDefinition<*>) {
            val xsd = Literals.XSD.fromClass(property.datatype)
            if (xsd != Literals.XSD.INT && xsd != Literals.XSD.DECIMAL && xsd != Literals.XSD.FLOAT) {
                throw InvalidDatatype(property.datatype, Literals.XSD.INT.`class`, Literals.XSD.DECIMAL.`class`, Literals.XSD.FLOAT.`class`)
            }
            val minInclusive = property.minInclusive
            val maxInclusive = property.maxInclusive
            if (minInclusive != null && maxInclusive != null && minInclusive.toDouble() > maxInclusive.toDouble()) {
                throw InvalidBounds(minInclusive, maxInclusive)
            }
        }
        predicateRepository.findById(property.path).orElseThrow { PredicateNotFound(property.path) }
        val range = when (property) {
            is LiteralTemplatePropertyDefinition -> property.datatype
            is ResourceTemplatePropertyDefinition -> property.`class`
            else -> null
        }
        if (range != null) {
            classRepository.findById(range).orElseThrow { ClassNotFound.withThingId(range) }
        }
    }
}
