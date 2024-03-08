package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.TemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertyValueCreator(
    resourceService: ResourceUseCases,
    literalService: LiteralUseCases,
    statementService: StatementUseCases
) : TemplatePropertyCreator(resourceService, literalService, statementService), TemplatePropertyAction {
    override fun invoke(command: CreateTemplatePropertyCommand, state: State): State {
        return state.copy(
            templatePropertyId = create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = state.propertyCount!! + 1,
                property = command
            ),
            propertyCount = state.propertyCount + 1
        )
    }
}
