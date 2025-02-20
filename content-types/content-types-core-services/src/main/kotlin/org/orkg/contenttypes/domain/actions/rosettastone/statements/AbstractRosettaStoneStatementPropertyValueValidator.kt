package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.MissingInputPositions
import org.orkg.contenttypes.domain.MissingObjectPositionValue
import org.orkg.contenttypes.domain.MissingPropertyValues
import org.orkg.contenttypes.domain.MissingSubjectPositionValue
import org.orkg.contenttypes.domain.NestedRosettaStoneStatement
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.NumberTooHigh
import org.orkg.contenttypes.domain.NumberTooLow
import org.orkg.contenttypes.domain.ObjectPositionValueDoesNotMatchPattern
import org.orkg.contenttypes.domain.ObjectPositionValueTooHigh
import org.orkg.contenttypes.domain.ObjectPositionValueTooLow
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.RosettaStoneStatementVersionNotFound
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.TooManyInputPositions
import org.orkg.contenttypes.domain.TooManyObjectPositionValues
import org.orkg.contenttypes.domain.TooManyPropertyValues
import org.orkg.contenttypes.domain.TooManySubjectPositionValues
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValueValidator
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.domain.actions.toThingDefinition
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.ThingDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import kotlin.math.absoluteValue

class AbstractRosettaStoneStatementPropertyValueValidator(
    override val thingRepository: ThingRepository,
    private val statementRepository: StatementRepository,
    private val rosettaStoneStatementService: RosettaStoneStatementUseCases,
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator,
) : ThingIdValidator {
    constructor(
        thingRepository: ThingRepository,
        statementRepository: StatementRepository,
        rosettaStoneStatementService: RosettaStoneStatementUseCases,
        classHierarchyRepository: ClassHierarchyRepository,
    ) : this(
        thingRepository,
        statementRepository,
        rosettaStoneStatementService,
        AbstractTemplatePropertyValueValidator(classHierarchyRepository)
    )

    fun validate(
        templateProperties: List<TemplateProperty>,
        thingDefinitions: Map<String, ThingDefinition>,
        validatedIdsIn: Map<String, Either<String, Thing>>,
        tempIds: Set<String>,
        templateId: ThingId,
        subjects: List<String>,
        objects: List<List<String>>,
    ): Map<String, Either<String, Thing>> {
        val validatedIds = validatedIdsIn.toMutableMap()
        validateInputPositionCount(templateId, objects, templateProperties)
        val inputs = objects.toMutableList()
        inputs.add(templateProperties.indexOfFirst { it.path.id == Predicates.hasSubjectPosition }, subjects)

        templateProperties.forEachIndexed { index, property ->
            val propertyInstances = inputs[index]
            try {
                abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances)
            } catch (e: MissingPropertyValues) {
                if (property.path.id == Predicates.hasSubjectPosition) {
                    throw MissingSubjectPositionValue(property.placeholder ?: property.label, property.minCount!!)
                } else {
                    throw MissingObjectPositionValue(property.placeholder ?: property.label, property.minCount!!)
                }
            } catch (e: TooManyPropertyValues) {
                if (property.path.id == Predicates.hasSubjectPosition) {
                    throw TooManySubjectPositionValues(property.placeholder ?: property.label, property.maxCount!!)
                } else {
                    throw TooManyObjectPositionValues(property.placeholder ?: property.label, property.maxCount!!)
                }
            }
            propertyInstances.forEach { objectId ->
                val `object` = validateId(objectId, tempIds, validatedIds)

                `object`.onLeft { tempId ->
                    validateObject(property, tempId, thingDefinitions[tempId]!!)
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
                    validateObject(property, thing.id.value, thing.toThingDefinition(statementRepository))
                }
            }
        }

        return validatedIds
    }

    private fun validateObject(property: TemplateProperty, id: String, `object`: ThingDefinition) {
        try {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        } catch (e: LabelDoesNotMatchPattern) {
            throw e.takeIf { property.path.id == Predicates.hasSubjectPosition }
                ?: ObjectPositionValueDoesNotMatchPattern(
                    positionPlaceholder = property.placeholder ?: property.label,
                    label = `object`.label,
                    pattern = (property as StringLiteralTemplateProperty).pattern!!
                )
        } catch (e: NumberTooLow) {
            throw e.takeIf { property.path.id == Predicates.hasSubjectPosition }
                ?: ObjectPositionValueTooLow(
                    positionPlaceholder = property.placeholder ?: property.label,
                    label = `object`.label,
                    minInclusive = (property as NumberLiteralTemplateProperty).minInclusive!!
                )
        } catch (e: NumberTooHigh) {
            throw e.takeIf { property.path.id == Predicates.hasSubjectPosition }
                ?: ObjectPositionValueTooHigh(
                    positionPlaceholder = property.placeholder ?: property.label,
                    label = `object`.label,
                    maxInclusive = (property as NumberLiteralTemplateProperty).maxInclusive!!
                )
        }
    }

    private fun validateInputPositionCount(templateId: ThingId, objects: List<List<String>>, templateProperties: List<TemplateProperty>) {
        val objectPositionDifference = objects.size - templateProperties.size + 1
        if (objectPositionDifference < 0) {
            throw MissingInputPositions(templateProperties.size, templateId, objectPositionDifference.absoluteValue)
        } else if (objectPositionDifference > 0) {
            throw TooManyInputPositions(templateProperties.size, templateId)
        }
    }
}
