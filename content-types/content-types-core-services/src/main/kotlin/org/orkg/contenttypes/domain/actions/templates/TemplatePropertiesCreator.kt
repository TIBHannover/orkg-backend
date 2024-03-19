package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertiesCreator(
    resourceService: ResourceUseCases,
    literalService: LiteralUseCases,
    statementService: StatementUseCases
) : AbstractTemplatePropertyCreator(resourceService, literalService, statementService), CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.properties.forEachIndexed { index, property ->
            create(command.contributorId, state.templateId!!, index + 1, property)
        }
        return state
    }
}
