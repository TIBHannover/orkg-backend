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
import org.orkg.contenttypes.domain.actions.toThingCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository

class AbstractRosettaStoneStatementPropertyValueValidator(
    private val thingIdValidator: ThingIdValidator,
    private val statementRepository: StatementRepository,
    private val rosettaStoneStatementService: RosettaStoneStatementUseCases,
    private val abstractTemplatePropertyValueValidator: AbstractTemplatePropertyValueValidator,
) {
    constructor(
        thingRepository: ThingRepository,
        statementRepository: StatementRepository,
        rosettaStoneStatementService: RosettaStoneStatementUseCases,
        classHierarchyRepository: ClassHierarchyRepository,
    ) : this(
        ThingIdValidator(thingRepository),
        statementRepository,
        rosettaStoneStatementService,
        AbstractTemplatePropertyValueValidator(classHierarchyRepository)
    )

    fun validate(
        templateProperties: List<TemplateProperty>,
        thingCommands: Map<String, CreateThingCommandPart>,
        validationCacheIn: Map<String, Either<CreateThingCommandPart, Thing>>,
        templateId: ThingId,
        subjects: List<String>,
        objects: List<List<String>>,
    ): Map<String, Either<CreateThingCommandPart, Thing>> {
        val validataionCache = validationCacheIn.toMutableMap()
        validateInputPositionCount(templateId, objects, templateProperties)
        val inputs = objects.toMutableList()
        inputs.add(templateProperties.indexOfFirst { it.path.id == Predicates.hasSubjectPosition }, subjects)

        templateProperties.forEachIndexed { index, property ->
            val propertyInstances = inputs[index]
            try {
                abstractTemplatePropertyValueValidator.validateCardinality(property, propertyInstances)
            } catch (_: MissingPropertyValues) {
                if (property.path.id == Predicates.hasSubjectPosition) {
                    throw MissingSubjectPositionValue(property.placeholder ?: property.label, property.minCount!!)
                } else {
                    throw MissingObjectPositionValue(property.placeholder ?: property.label, index, property.minCount!!)
                }
            } catch (_: TooManyPropertyValues) {
                if (property.path.id == Predicates.hasSubjectPosition) {
                    throw TooManySubjectPositionValues(property.placeholder ?: property.label, property.maxCount!!)
                } else {
                    throw TooManyObjectPositionValues(property.placeholder ?: property.label, index, property.maxCount!!)
                }
            }
            propertyInstances.forEachIndexed { valueIndex, objectId ->
                val `object` = thingIdValidator.validate(objectId, thingCommands, validataionCache)

                `object`.onLeft { command ->
                    validateObject(property, index, valueIndex, objectId, command)
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
                    validateObject(property, index, valueIndex, thing.id.value, thing.toThingCommandPart(statementRepository))
                }
            }
        }
        return validataionCache
    }

    private fun validateObject(property: TemplateProperty, positionIndex: Int, valueIndex: Int, id: String, `object`: CreateThingCommandPart) {
        try {
            abstractTemplatePropertyValueValidator.validateObject(property, id, `object`)
        } catch (e: LabelDoesNotMatchPattern) {
            throw e.takeIf { property.path.id == Predicates.hasSubjectPosition }
                ?: ObjectPositionValueDoesNotMatchPattern(
                    positionPlaceholder = property.placeholder ?: property.label,
                    objectPositionIndex = positionIndex,
                    label = `object`.label,
                    labelIndex = valueIndex,
                    pattern = (property as StringLiteralTemplateProperty).pattern!!
                )
        } catch (e: NumberTooLow) {
            throw e.takeIf { property.path.id == Predicates.hasSubjectPosition }
                ?: ObjectPositionValueTooLow(
                    positionPlaceholder = property.placeholder ?: property.label,
                    objectPositionIndex = positionIndex,
                    label = `object`.label,
                    labelIndex = valueIndex,
                    minInclusive = (property as NumberLiteralTemplateProperty).minInclusive!!
                )
        } catch (e: NumberTooHigh) {
            throw e.takeIf { property.path.id == Predicates.hasSubjectPosition }
                ?: ObjectPositionValueTooHigh(
                    positionPlaceholder = property.placeholder ?: property.label,
                    objectPositionIndex = positionIndex,
                    label = `object`.label,
                    labelIndex = valueIndex,
                    maxInclusive = (property as NumberLiteralTemplateProperty).maxInclusive!!
                )
        }
    }

    private fun validateInputPositionCount(templateId: ThingId, objects: List<List<String>>, templateProperties: List<TemplateProperty>) {
        val objectPositionDifference = objects.size - templateProperties.size + 1
        if (objectPositionDifference < 0) {
            throw MissingInputPositions(templateProperties.size, objects.size + 1, templateId)
        } else if (objectPositionDifference > 0) {
            throw TooManyInputPositions(templateProperties.size, objects.size + 1, templateId)
        }
    }
}
