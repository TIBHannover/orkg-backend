package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.MissingDynamicLabelPlaceholder
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateLabelSectionsMustBeOptional
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustBeUpdated
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustStartWithPreviousVersion
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.TooManyNewRosettaStoneTemplateLabelSections
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.contenttypes.input.TemplatePropertyCommand
import org.orkg.graph.domain.DynamicLabel.PlaceholderComponent
import org.orkg.graph.domain.DynamicLabel.SectionComponent
import org.orkg.graph.domain.DynamicLabel.TextComponent

class RosettaStoneTemplateDynamicLabelUpdateValidator : UpdateRosettaStoneTemplateAction {
    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        if (command.dynamicLabel != null && command.dynamicLabel != state.rosettaStoneTemplate!!.dynamicLabel) {
            val properties = command.properties ?: state.rosettaStoneTemplate.properties
            val newDynamicLabel = command.dynamicLabel!!
            val keys = newDynamicLabel.components.mapNotNullTo(mutableSetOf()) { component ->
                when (component) {
                    is PlaceholderComponent -> component.key
                    is SectionComponent -> component.key
                    is TextComponent -> null
                }
            }
            val requiredKeys = properties.indices.map(Int::toString)
            val missingKeys = requiredKeys - keys
            if (missingKeys.isNotEmpty()) {
                val propertyIndex = missingKeys.first().toInt()
                val placeholder = properties[propertyIndex].let { property ->
                    when (property) {
                        is TemplateProperty -> property.placeholder
                        is TemplatePropertyCommand -> property.placeholder
                        else -> null
                    }
                }
                throw MissingDynamicLabelPlaceholder(propertyIndex, placeholder)
            }
            if (state.isUsedInRosettaStoneStatement) {
                if (command.properties == null || command.properties!!.size <= state.rosettaStoneTemplate.properties.size) {
                    throw RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties()
                }
                val oldDynamicLabel = state.rosettaStoneTemplate.dynamicLabel
                if (newDynamicLabel.components.size < oldDynamicLabel.components.size) {
                    throw RosettaStoneTemplateLabelMustStartWithPreviousVersion()
                }
                oldDynamicLabel.components.forEachIndexed { index, oldComponent ->
                    val newComponent = newDynamicLabel.components[index]
                    if (newComponent != oldComponent) {
                        throw RosettaStoneTemplateLabelMustStartWithPreviousVersion()
                    }
                }
                val newComponents = newDynamicLabel.components.drop(oldDynamicLabel.components.size)
                if (newComponents.size > command.properties!!.size - state.rosettaStoneTemplate.properties.size) {
                    throw TooManyNewRosettaStoneTemplateLabelSections()
                }
                if (newComponents.any { it !is SectionComponent }) {
                    throw NewRosettaStoneTemplateLabelSectionsMustBeOptional()
                }
            }
        } else if (state.isUsedInRosettaStoneStatement && command.properties!!.size != state.rosettaStoneTemplate!!.properties.size) {
            throw RosettaStoneTemplateLabelMustBeUpdated()
        }
        return state
    }
}
