package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.MissingInputPositions
import org.orkg.contenttypes.domain.NestedRosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.RosettaStoneStatementVersionNotFound
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.TooManyInputPositions
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValueValidator
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.domain.actions.toThingDefinition
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.ThingDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository

class AbstractRosettaStoneStatementPropertyValueValidator(
    override val thingRepository: ThingRepository,
    private val statementRepository: StatementRepository,
    private val rosettaStoneStatementService: RosettaStoneStatementUseCases,
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator = AbstractTemplatePropertyValueValidator()
) : ThingIdValidator {
    fun validate(
        templateProperties: List<TemplateProperty>,
        thingDefinitions: Map<String, ThingDefinition>,
        validatedIdsIn: Map<String, Either<String, Thing>>,
        tempIds: Set<String>,
        templateId: ThingId,
        subjects: List<String>,
        objects: List<List<String>>
    ): Map<String, Either<String, Thing>> {
        val validatedIds = validatedIdsIn.toMutableMap()
        validateInputPositionCount(templateId, objects, templateProperties)
        val inputs = objects.toMutableList()
        inputs.add(templateProperties.indexOfFirst { it.path.id == Predicates.hasSubjectPosition }, subjects)

        templateProperties.forEachIndexed { index, property ->
            val propertyInstances = inputs[index]
            abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances)
            propertyInstances.forEach { objectId ->
                val `object` = validateId(objectId, tempIds, validatedIds)

                `object`.onLeft { tempId ->
                    abstractTemplatePropertyValueValidator.validateObject(property, tempId, thingDefinitions[tempId]!!)
                }

                `object`.onRight { thing ->
                    if (thing is Resource && Classes.rosettaStoneStatement in thing.classes) {
                        val statementVersion = rosettaStoneStatementService.findByIdOrVersionId(thing.id)
                            .orElseThrow { RosettaStoneStatementNotFound(thing.id) }
                            .findVersionById(thing.id) ?: throw RosettaStoneStatementVersionNotFound(thing.id)
                        val resourceInputs = statementVersion.allInputs.filterIsInstance<Resource>()
                        if (resourceInputs.any { Classes.rosettaStoneStatement in it.classes }) {
                            throw NestedRosettaStoneStatement(thing.id, index)
                        }
                    }
                    abstractTemplatePropertyValueValidator.validateObject(property, thing.id.value, thing.toThingDefinition(statementRepository))
                }
            }
        }

        return validatedIds
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
