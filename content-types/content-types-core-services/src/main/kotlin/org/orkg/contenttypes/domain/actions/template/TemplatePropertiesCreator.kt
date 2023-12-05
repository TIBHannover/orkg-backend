package org.orkg.contenttypes.domain.actions.template

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.TemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertiesCreator(
    resourceService: ResourceUseCases,
    literalService: LiteralUseCases,
    statementService: StatementUseCases
) : TemplatePropertyCreator(resourceService, literalService, statementService), TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.properties.forEachIndexed { index, property ->
            create(command.contributorId, state.templateId!!, index + 1, property)
        }
        return state
    }
}
