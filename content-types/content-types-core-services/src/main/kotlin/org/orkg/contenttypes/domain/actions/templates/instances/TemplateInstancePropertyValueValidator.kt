package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.UnknownTemplateProperties
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValueValidator
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.contenttypes.domain.actions.toThingCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.graph.domain.Literals
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository

class TemplateInstancePropertyValueValidator(
    override val thingRepository: ThingRepository,
    private val classRepository: ClassRepository,
    private val statementRepository: StatementRepository,
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator,
) : UpdateTemplateInstanceAction,
    ThingIdValidator {
    constructor(
        thingRepository: ThingRepository,
        classRepository: ClassRepository,
        statementRepository: StatementRepository,
        classHierarchyRepository: ClassHierarchyRepository,
    ) : this(
        thingRepository,
        classRepository,
        statementRepository,
        AbstractTemplatePropertyValueValidator(classHierarchyRepository)
    )

    override fun invoke(
        command: UpdateTemplateInstanceCommand,
        state: UpdateTemplateInstanceState,
    ): UpdateTemplateInstanceState {
        val toRemove = mutableSetOf<BakedStatement>()
        val toAdd = mutableSetOf<BakedStatement>()
        val templateInstance = state.templateInstance!!
        val validatedIds = state.validatedIds.toMutableMap()
        val thingsCommand = command.all()
        val literalCommands = mutableMapOf<String, CreateLiteralCommandPart>()

        validatePropertyPaths(state.template!!, command.statements.keys)

        state.template.properties.forEach { property ->
            val propertyInstances = command.statements[property.path.id].orEmpty()
            abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances)
            toRemove += templateInstance.statements[property.path.id]!!.map {
                BakedStatement(templateInstance.root.id.value, property.path.id.value, it.thing.id.value)
            }
            propertyInstances.forEach { objectId ->
                val `object` = validateId(objectId, state.tempIds, validatedIds)

                `object`.onLeft { tempId ->
                    val thingCommand = thingsCommand[tempId]!!
                    abstractTemplatePropertyValueValidator.validateObject(property, tempId, thingCommand)
                    if (property is LiteralTemplateProperty && thingCommand is CreateLiteralCommandPart) {
                        literalCommands[tempId] = thingCommand.copy(
                            dataType = Literals.XSD.fromClass(property.datatype.id)?.prefixedUri
                                ?: classRepository.findById(property.datatype.id).orElse(null)?.uri?.toString()
                                ?: Literals.XSD.STRING.prefixedUri
                        )
                    }
                    toAdd += BakedStatement(templateInstance.root.id.value, property.path.id.value, tempId)
                }

                `object`.onRight { thing ->
                    abstractTemplatePropertyValueValidator.validateObject(property, thing.id.value, thing.toThingCommandPart(statementRepository))
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
            literals = literalCommands
        )
    }

    private fun validatePropertyPaths(template: Template, properties: Set<ThingId>) {
        val templatePropertyPaths = template.properties.map { it.path.id }
        val unknownProperties = properties - (properties.intersect(templatePropertyPaths.toSet()))

        if (unknownProperties.isNotEmpty()) {
            throw UnknownTemplateProperties(template.id, unknownProperties)
        }
    }
}
