package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.CreateTemplatePropertyAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertyCreator(
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator
) : CreateTemplatePropertyAction {
    constructor(
        resourceService: ResourceUseCases,
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(AbstractTemplatePropertyCreator(resourceService, literalService, statementService))

    override fun invoke(command: CreateTemplatePropertyCommand, state: State): State {
        return state.copy(
            templatePropertyId = abstractTemplatePropertyCreator.create(
                contributorId = command.contributorId,
                templateId = command.templateId,
                order = state.propertyCount!! + 1,
                property = command
            ),
            propertyCount = state.propertyCount + 1
        )
    }
}
