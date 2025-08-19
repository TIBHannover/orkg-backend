package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.NewRosettaStoneTemplatePropertyMustBeOptional
import org.orkg.contenttypes.domain.RosettaStoneTemplatePropertyNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class RosettaStoneTemplatePropertiesUpdateValidator(
    private val abstractRosettaStoneTemplatePropertiesValidator: AbstractRosettaStoneTemplatePropertiesValidator,
) : UpdateRosettaStoneTemplateAction {
    constructor(
        predicateRepository: PredicateRepository,
        classRepository: ClassRepository,
    ) : this(
        AbstractRosettaStoneTemplatePropertiesValidator(predicateRepository, classRepository)
    )

    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        command.properties?.also { newProperties ->
            if (state.isUsedInRosettaStoneStatement) {
                val oldProperties = state.rosettaStoneTemplate!!.properties
                if (newProperties.size < oldProperties.size) {
                    throw RosettaStoneTemplatePropertyNotModifiable(oldProperties[newProperties.size].id)
                }
                newProperties.forEachIndexed { index, newProperty ->
                    if (index < oldProperties.size) {
                        val oldProperty = oldProperties[index]
                        if (!newProperty.matchesProperty(oldProperty)) {
                            throw RosettaStoneTemplatePropertyNotModifiable(oldProperty.id)
                        }
                    } else if (newProperty.minCount != null && newProperty.minCount!! > 0) {
                        val placeholder = newProperty.placeholder ?: newProperty.label
                        throw NewRosettaStoneTemplatePropertyMustBeOptional(index, placeholder)
                    }
                }
            }
            abstractRosettaStoneTemplatePropertiesValidator.validate(newProperties)
        }
        return state
    }
}
