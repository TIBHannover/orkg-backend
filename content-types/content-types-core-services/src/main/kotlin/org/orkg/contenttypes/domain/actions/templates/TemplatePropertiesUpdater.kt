package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertiesUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertiesUpdater(
    private val abstractTemplatePropertiesUpdater: AbstractTemplatePropertiesUpdater
) : UpdateTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
    ) : this(AbstractTemplatePropertiesUpdater(literalService, resourceService, statementService))

    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        command.properties?.let { properties ->
            abstractTemplatePropertiesUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                newProperties = properties,
                oldProperties = state.template!!.properties,
                statements = state.statements
            )
        }
        return state
    }
}
