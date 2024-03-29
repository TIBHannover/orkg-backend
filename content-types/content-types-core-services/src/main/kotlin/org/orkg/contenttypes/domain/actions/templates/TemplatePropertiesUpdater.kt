package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyDeleter
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertiesUpdater(
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator,
    private val abstractTemplatePropertyUpdater: AbstractTemplatePropertyUpdater,
    private val abstractTemplatePropertyDeleter: AbstractTemplatePropertyDeleter
) : UpdateTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
    ) : this(
        object : AbstractTemplatePropertyCreator(resourceService, literalService, statementService) {},
        AbstractTemplatePropertyUpdater(literalService, resourceService, statementService),
        AbstractTemplatePropertyDeleter(resourceService, statementService)
    )

    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        command.properties?.let { properties ->
            val oldProperties = state.template!!.properties.toMutableList()
            val new2old = properties.associateWith { newProperty ->
                oldProperties.firstOrNull { newProperty.matchesProperty(it) }?.also { oldProperties.remove(it) }
            }
            properties.forEachIndexed { index, newProperty ->
                val oldProperty = new2old[newProperty]
                val order = index + 1
                if (oldProperty != null) {
                    if (oldProperty.order != order.toLong()) {
                        abstractTemplatePropertyUpdater.update(
                            contributorId = command.contributorId,
                            order = order,
                            newProperty = newProperty,
                            oldProperty = oldProperty
                        )
                    }
                } else {
                    abstractTemplatePropertyCreator.create(
                        contributorId = command.contributorId,
                        templateId = command.templateId,
                        order = order,
                        property = newProperty
                    )
                }
            }
            oldProperties.forEach {
                abstractTemplatePropertyDeleter.delete(command.contributorId, command.templateId, it.id)
            }
        }
        return state
    }
}
