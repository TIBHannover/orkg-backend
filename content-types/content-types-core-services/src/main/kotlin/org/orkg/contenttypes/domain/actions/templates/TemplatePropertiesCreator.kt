package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertiesCreator(
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator
) : CreateTemplateAction {
    constructor(
        resourceService: ResourceUseCases,
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(AbstractTemplatePropertyCreator(resourceService, literalService, statementService))

    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.properties.forEachIndexed { index, property ->
            abstractTemplatePropertyCreator.create(command.contributorId, state.templateId!!, index, property)
        }
        return state
    }
}
