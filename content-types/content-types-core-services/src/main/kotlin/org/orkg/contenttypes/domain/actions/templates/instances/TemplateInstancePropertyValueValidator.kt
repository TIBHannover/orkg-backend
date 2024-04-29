package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidLiteral
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.LiteralTemplateProperty
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
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.TooManyPropertyValues
import org.orkg.contenttypes.domain.UnknownTemplateProperties
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.actions.toThingDefinition
import org.orkg.contenttypes.input.ClassDefinition
import org.orkg.contenttypes.input.ListDefinition
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.ThingDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository

class TemplateInstancePropertyValueValidator(
    override val thingRepository: ThingRepository,
    private val classRepository: ClassRepository
) : UpdateTemplateInstanceAction, ThingIdValidator {
    override fun invoke(
        command: UpdateTemplateInstanceCommand,
        state: UpdateTemplateInstanceState
    ): UpdateTemplateInstanceState {
        val toRemove = mutableSetOf<BakedStatement>()
        val toAdd = mutableSetOf<BakedStatement>()
        val templateInstance = state.templateInstance!!
        val validatedIds = state.validatedIds.toMutableMap()
        val thingDefinitions = command.all()
        val literalDefinitions = mutableMapOf<String, LiteralDefinition>()

        validatePropertyPaths(state.template!!, command.statements.keys)

        state.template.properties.forEach { property ->
            val propertyInstances = command.statements[property.path.id].orEmpty()
            validateCardinality(property, propertyInstances)
            toRemove += templateInstance.statements[property.path.id]!!.map {
                BakedStatement(templateInstance.root.id.value, property.path.id.value, it.thing.id.value)
            }
            propertyInstances.forEach { objectId ->
                val `object` = validateId(objectId, state.tempIds, validatedIds)

                `object`.onLeft { tempId ->
                    val thingDefinition = thingDefinitions[tempId]!!
                    validateObject(property, tempId, thingDefinition)
                    if (property is LiteralTemplateProperty && thingDefinition is LiteralDefinition) {
                        literalDefinitions[tempId] = thingDefinition.copy(
                            dataType = Literals.XSD.fromClass(property.datatype.id)?.prefixedUri
                                ?: classRepository.findById(property.datatype.id).orElse(null)?.uri?.toString()
                                ?: Literals.XSD.STRING.prefixedUri
                        )
                    }
                    toAdd += BakedStatement(templateInstance.root.id.value, property.path.id.value, tempId)
                }

                `object`.onRight { thing ->
                    validateObject(property, thing.id.value, thing.toThingDefinition())
                    val statement = BakedStatement(templateInstance.root.id.value, property.path.id.value, thing.id.value)
                    if (statement in toRemove) {
                        toRemove -= statement
                    } else {
                        toAdd += statement
                    }
                }
            }
        }

        return state.copy(
            validatedIds = validatedIds,
            statementsToAdd = toAdd,
            statementsToRemove = toRemove,
            literals = literalDefinitions
        )
    }

    private fun validatePropertyPaths(template: Template, properties: Set<ThingId>) {
        val templatePropertyPaths = template.properties.map { it.path.id }
        val unknownProperties = properties - (properties.intersect(templatePropertyPaths.toSet()))

        if (unknownProperties.isNotEmpty()) {
            throw UnknownTemplateProperties(template.id, unknownProperties)
        }
    }

    private fun validateCardinality(property: TemplateProperty, propertyInstances: List<String>) {
        if (property.minCount != null && property.minCount!! > 0 && propertyInstances.size < property.minCount!!) {
            throw MissingPropertyValues(property.id, property.path.id, property.minCount!!, propertyInstances.size)
        }
        if (property.maxCount != null && property.maxCount!! > 0 && propertyInstances.size > property.maxCount!!) {
            throw TooManyPropertyValues(property.id, property.path.id, property.maxCount!!, propertyInstances.size)
        }
    }

    private fun validateObject(property: TemplateProperty, id: String, `object`: ThingDefinition) {
        validateObjectTyping(property, id, `object`)
        validateObjectLabel(property, id, `object`.label)
    }

    private fun validateObjectTyping(property: TemplateProperty, id: String, `object`: ThingDefinition) {
        when (property) {
            is ResourceTemplateProperty -> {
                if (property.`class`.id == Classes.classes && `object` !is ClassDefinition) {
                    throw ObjectIsNotAClass(property.id, property.path.id, id)
                } else if (property.`class`.id == Classes.predicates && `object` !is PredicateDefinition) {
                    throw ObjectIsNotAPredicate(property.id, property.path.id, id)
                } else if (property.`class`.id == Classes.list && `object` !is ListDefinition) {
                    throw ObjectIsNotAList(property.id, property.path.id, id)
                } else if (`object` is LiteralDefinition) {
                    throw ObjectMustNotBeALiteral(property.id, property.path.id, id)
                } else if (`object` is ResourceDefinition && property.`class`.id != Classes.resources && property.`class`.id !in `object`.classes) {
                    throw ResourceIsNotAnInstanceOfTargetClass(property.id, property.path.id, id, property.`class`.id)
                }
            }
            is LiteralTemplateProperty -> {
                if (`object` !is LiteralDefinition) {
                    throw ObjectIsNotALiteral(property.id, property.path.id, id)
                }
                Literals.XSD.fromClass(property.datatype.id)?.let { type ->
                    if (!type.canParse(`object`.label)) {
                        throw InvalidLiteral(property.id, property.path.id, property.datatype.id, id, `object`.label)
                    }
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
        } else if (property is NumberLiteralTemplateProperty<*>) {
            property.minInclusive?.let { minInclusive ->
                val invalid = when (property.datatype.id) {
                    Classes.decimal -> label.toDouble() < minInclusive.toDouble()
                    Classes.integer -> label.toInt() < minInclusive.toInt()
                    Classes.float -> label.toFloat() < minInclusive.toFloat()
                    else -> throw IllegalStateException("""Encountered number literal template property "${property.id}" with invalid datatype "${property.datatype}". This is a bug!""")
                }
                if (invalid) {
                    throw NumberTooLow(property.id, objectId, property.path.id, label, minInclusive)
                }
            }
            property.maxInclusive?.let { maxInclusive ->
                val invalid = when (property.datatype.id) {
                    Classes.decimal -> maxInclusive.toDouble() < label.toDouble()
                    Classes.integer -> maxInclusive.toInt() < label.toInt()
                    Classes.float -> maxInclusive.toFloat() < label.toFloat()
                    else -> throw IllegalStateException("""Encountered number literal template property "${property.id}" with invalid datatype "${property.datatype}". This is a bug!""")
                }
                if (invalid) {
                    throw NumberTooHigh(property.id, objectId, property.path.id, label, maxInclusive)
                }
            }
        }
    }
}
