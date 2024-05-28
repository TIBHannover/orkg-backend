package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.InvalidObjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionCardinality
import org.orkg.contenttypes.domain.InvalidSubjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionType
import org.orkg.contenttypes.domain.MissingPropertyPlaceholder
import org.orkg.contenttypes.domain.MissingSubjectPosition
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
        if (command.properties.isEmpty()) {
            throw MissingSubjectPosition()
        }

        command.properties.first().let { subject ->
            if (subject.path != Predicates.hasSubjectPosition) {
                throw InvalidSubjectPositionPath()
            }
            if (subject.minCount == null || subject.minCount!! < 1) {
                throw InvalidSubjectPositionCardinality()
            }
            if (subject is LiteralTemplatePropertyDefinition) {
                throw InvalidSubjectPositionType()
            }
        }

        command.properties.withIndex().drop(1).forEach { (index, `object`) ->
            if (`object`.path != Predicates.hasObjectPosition) {
                throw InvalidObjectPositionPath(index)
            }
        }

        command.properties.forEachIndexed { index, property ->
            if (property.placeholder == null) {
                throw MissingPropertyPlaceholder(index)
            }
            abstractTemplatePropertyValidator.validate(property)
        }

        return state
    }
}
