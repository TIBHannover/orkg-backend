package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.InvalidSubjectPositionCardinality
import org.orkg.contenttypes.domain.InvalidSubjectPositionType
import org.orkg.contenttypes.domain.MissingPropertyPlaceholder
import org.orkg.contenttypes.domain.MissingRequiredObjectPosition
import org.orkg.contenttypes.domain.MissingSubjectPosition
import org.orkg.contenttypes.domain.TooManySubjectPositions
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.input.LiteralTemplatePropertyDefinition
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class RosettaStoneTemplatePropertiesValidator(
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator
) : CreateRosettaStoneTemplateAction {
    constructor(
        predicateRepository: PredicateRepository,
        classRepository: ClassRepository,
    ) : this(AbstractTemplatePropertyValidator(predicateRepository, classRepository))

    override fun invoke(
        command: CreateRosettaStoneTemplateCommand,
        state: CreateRosettaStoneTemplateState
    ): CreateRosettaStoneTemplateState {
        var subjectCount = 0
        var requiredObjectCount = 0
        command.properties.forEachIndexed { index, property ->
            if (property.path == Predicates.hasSubjectPosition) {
                if (subjectCount > 0) {
                    throw TooManySubjectPositions()
                }
                if (property.minCount == null || property.minCount!! < 1) {
                    throw InvalidSubjectPositionCardinality()
                }
                if (property is LiteralTemplatePropertyDefinition) {
                    throw InvalidSubjectPositionType()
                }
                subjectCount++
            } else if (property.path == Predicates.hasObjectPosition) {
                if (property.minCount != null && property.minCount!! > 0) {
                    requiredObjectCount++
                }
            }
            if (property.placeholder == null) {
                throw MissingPropertyPlaceholder(index)
            }
            abstractTemplatePropertyValidator.validate(property)
        }
        if (subjectCount == 0) {
            throw MissingSubjectPosition()
        }
        if (requiredObjectCount == 0) {
            throw MissingRequiredObjectPosition()
        }
        return state
    }
}
