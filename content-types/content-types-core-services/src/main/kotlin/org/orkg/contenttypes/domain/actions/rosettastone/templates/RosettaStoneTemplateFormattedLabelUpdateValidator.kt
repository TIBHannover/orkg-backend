package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.MissingFormattedLabelPlaceholder
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateLabelSectionsMustBeOptional
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustBeUpdated
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustStartWithPreviousVersion
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.TooManyNewRosettaStoneTemplateLabelSections
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.domain.DynamicLabel
import org.orkg.graph.domain.DynamicLabel.PlaceholderComponent
import org.orkg.graph.domain.DynamicLabel.SectionComponent
import org.orkg.graph.domain.DynamicLabel.TextComponent

class RosettaStoneTemplateFormattedLabelUpdateValidator : UpdateRosettaStoneTemplateAction {
    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        if (command.formattedLabel != null && command.formattedLabel != state.rosettaStoneTemplate!!.formattedLabel) {
            val properties = command.properties ?: state.rosettaStoneTemplate.properties
            val newDynamicLabel = DynamicLabel(command.formattedLabel!!.value)
            val keys = newDynamicLabel.components.mapNotNullTo(mutableSetOf()) { component ->
                when (component) {
                    is PlaceholderComponent -> component.key
                    is SectionComponent -> component.key
                    is TextComponent -> null
                }
            }
            val requiredKeys = (0 until properties.size).map(Int::toString)
            val missingKeys = requiredKeys - keys
            if (missingKeys.isNotEmpty()) {
                val propertyIndex = missingKeys.first().toInt()
                val placeholder = properties[propertyIndex].let { property ->
                    when (property) {
                        is TemplateProperty -> property.placeholder
                        is TemplatePropertyDefinition -> property.placeholder
                        else -> null
                    }
                }
                if (placeholder != null) {
                    throw MissingFormattedLabelPlaceholder(placeholder)
                } else {
                    throw MissingFormattedLabelPlaceholder(propertyIndex)
                }
            }
            if (state.isUsedInRosettaStoneStatement) {
                if (command.properties == null || command.properties!!.size <= state.rosettaStoneTemplate.properties.size) {
                    throw RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties()
                }
                val oldDynamicLabel = DynamicLabel(state.rosettaStoneTemplate.formattedLabel.value)
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
