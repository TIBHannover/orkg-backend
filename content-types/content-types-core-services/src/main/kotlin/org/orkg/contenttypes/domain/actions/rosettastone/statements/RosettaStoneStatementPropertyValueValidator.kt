package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.MissingInputPositions
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.TooManyInputPositions
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValueValidator
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.contenttypes.domain.actions.toThingDefinition
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementPropertyValueValidator(
    override val thingRepository: ThingRepository,
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator = AbstractTemplatePropertyValueValidator()
) : CreateRosettaStoneStatementAction, ThingIdValidator {
    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
        val validatedIds = state.validatedIds.toMutableMap()
        val thingDefinitions = command.all()
        val templateProperties = state.rosettaStoneTemplate!!.properties

        validateInputPositionCount(command.templateId, command.objects, templateProperties)

        val inputs = command.objects.toMutableList()
        inputs.add(templateProperties.indexOfFirst { it.path.id == Predicates.hasSubjectPosition }, command.subjects)

        templateProperties.forEachIndexed { index, property ->
            val propertyInstances = inputs[index]
            abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances)
            propertyInstances.forEach { objectId ->
                val `object` = validateId(objectId, state.tempIds, validatedIds)

                `object`.onLeft { tempId ->
                    val thingDefinition = thingDefinitions[tempId]!!
                    abstractTemplatePropertyValueValidator.validateObject(property, tempId, thingDefinition)
                }

                `object`.onRight { thing ->
                    abstractTemplatePropertyValueValidator.validateObject(property, thing.id.value, thing.toThingDefinition())
                }
            }
        }

        return state.copy(validatedIds = validatedIds)
    }

    private fun validateInputPositionCount(templateId: ThingId, objects: List<List<String>>, templateProperties: List<TemplateProperty>) {
        val objectPositionDifference = objects.size - templateProperties.size + 1
        if (objectPositionDifference < 0) {
            throw MissingInputPositions(templateProperties.size, templateId, objectPositionDifference)
        } else if (objectPositionDifference > 0) {
            throw TooManyInputPositions(templateProperties.size, templateId)
        }
    }
}
