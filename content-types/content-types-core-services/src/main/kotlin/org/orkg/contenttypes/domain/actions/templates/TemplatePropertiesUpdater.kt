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
        AbstractTemplatePropertyCreator(resourceService, literalService, statementService),
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
                if (oldProperty != null) {
                    if (oldProperty.order != index.toLong()) {
                        abstractTemplatePropertyUpdater.update(
                            statements = state.statements[oldProperty.id].orEmpty(),
                            contributorId = command.contributorId,
                            order = index,
                            newProperty = newProperty,
                            oldProperty = oldProperty
                        )
                    }
                } else {
                    abstractTemplatePropertyCreator.create(
                        contributorId = command.contributorId,
                        templateId = command.templateId,
                        order = index,
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
