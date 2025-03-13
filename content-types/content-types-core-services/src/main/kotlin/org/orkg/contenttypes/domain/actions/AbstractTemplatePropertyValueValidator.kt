package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.common.toRealNumber
import org.orkg.contenttypes.domain.InvalidLiteral
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.MismatchedDataType
import org.orkg.contenttypes.domain.MissingPropertyValues
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.NumberTooHigh
import org.orkg.contenttypes.domain.NumberTooLow
import org.orkg.contenttypes.domain.ObjectIsNotAClass
import org.orkg.contenttypes.domain.ObjectIsNotAList
import org.orkg.contenttypes.domain.ObjectIsNotALiteral
import org.orkg.contenttypes.domain.ObjectIsNotAPredicate
import org.orkg.contenttypes.domain.ObjectMustNotBeALiteral
import org.orkg.contenttypes.domain.ResourceIsNotAnInstanceOfTargetClass
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.TooManyPropertyValues
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.contenttypes.input.CreateClassCommandPart
import org.orkg.contenttypes.input.CreateListCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.output.ClassHierarchyRepository

@Suppress("HttpUrlsUsage")
private const val ORKG_CLASS_NS = "http://orkg.org/orkg/class/"

class AbstractTemplatePropertyValueValidator(
    private val classHierarchyRepository: ClassHierarchyRepository,
) {
    internal fun validateCardinality(property: TemplateProperty, propertyInstances: List<String>) {
        if (property.minCount != null && property.minCount!! > 0 && propertyInstances.size < property.minCount!!) {
            throw MissingPropertyValues(property.id, property.path.id, property.minCount!!, propertyInstances.size)
        }
        if (property.maxCount != null && property.maxCount!! > 0 && propertyInstances.size > property.maxCount!!) {
            throw TooManyPropertyValues(property.id, property.path.id, property.maxCount!!, propertyInstances.size)
        }
    }

    internal fun validateObject(property: TemplateProperty, id: String, `object`: CreateThingCommandPart) {
        validateObjectTyping(property, id, `object`)
        validateObjectLabel(property, id, `object`.label)
    }

    private fun validateObjectTyping(property: TemplateProperty, id: String, `object`: CreateThingCommandPart) {
        when (property) {
            is ResourceTemplateProperty -> {
                if (property.`class`.id == Classes.classes && `object` !is CreateClassCommandPart) {
                    throw ObjectIsNotAClass(property.id, property.path.id, id)
                } else if (property.`class`.id == Classes.predicates && `object` !is CreatePredicateCommandPart) {
                    throw ObjectIsNotAPredicate(property.id, property.path.id, id)
                } else if (property.`class`.id == Classes.list && `object` !is CreateListCommandPart) {
                    throw ObjectIsNotAList(property.id, property.path.id, id)
                } else if (`object` is CreateLiteralCommandPart) {
                    throw ObjectMustNotBeALiteral(property.id, property.path.id, id)
                } else if (`object` is CreateResourceCommandPart && property.`class`.id != Classes.resources && !`object`.isInstanceOf(property.`class`.id)) {
                    throw ResourceIsNotAnInstanceOfTargetClass(property.id, property.path.id, id, property.`class`.id)
                }
            }
            is LiteralTemplateProperty -> {
                if (`object` !is CreateLiteralCommandPart) {
                    throw ObjectIsNotALiteral(property.id, property.path.id, id)
                }
                val xsd = Literals.XSD.fromClass(property.datatype.id)
                if (xsd != null) {
                    if (!xsd.canParse(`object`.label)) {
                        throw InvalidLiteral(property.id, property.path.id, property.datatype.id, id, `object`.label)
                    }
                } else if (property.datatype.uri?.toString() != `object`.dataType && "$ORKG_CLASS_NS${property.datatype.id}" != `object`.dataType) {
                    throw MismatchedDataType(
                        templatePropertyId = property.id,
                        predicateId = property.path.id,
                        expectedDataType = property.datatype.uri?.toString() ?: "$ORKG_CLASS_NS${property.datatype.id}",
                        id = id,
                        foundDataType = `object`.dataType
                    )
                }
            }
            is UntypedTemplateProperty -> {}
        }
    }

    private fun validateObjectLabel(property: TemplateProperty, objectId: String, label: String) {
        if (property is StringLiteralTemplateProperty) {
            property.pattern?.let { pattern ->
                if (!label.matches(Regex(pattern))) {
                    throw LabelDoesNotMatchPattern(property.id, objectId, property.path.id, label, pattern)
                }
            }
        } else if (property is NumberLiteralTemplateProperty) {
            property.minInclusive?.let { minInclusive ->
                val xsd = Literals.XSD.fromClass(property.datatype.id)
                if (xsd?.isNumber != true) {
                    throw IllegalStateException("""Encountered number literal template property "${property.id}" with invalid datatype "${property.datatype}". This is a bug!""")
                }
                if (label.toRealNumber() < minInclusive) {
                    throw NumberTooLow(property.id, objectId, property.path.id, label, minInclusive)
                }
            }
            property.maxInclusive?.let { maxInclusive ->
                val xsd = Literals.XSD.fromClass(property.datatype.id)
                if (xsd?.isNumber != true) {
                    throw IllegalStateException("""Encountered number literal template property "${property.id}" with invalid datatype "${property.datatype}". This is a bug!""")
                }
                if (maxInclusive < label.toRealNumber()) {
                    throw NumberTooHigh(property.id, objectId, property.path.id, label, maxInclusive)
                }
            }
        }
    }

    private fun CreateResourceCommandPart.isInstanceOf(targetClass: ThingId): Boolean = targetClass in classes || classes.any { `class` -> classHierarchyRepository.existsChild(targetClass, `class`) }
}
