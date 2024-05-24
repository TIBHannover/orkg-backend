package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.UpdateTemplatePropertyAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class TemplatePropertyUpdater(
    private val abstractTemplatePropertyUpdater: AbstractTemplatePropertyUpdater
) : UpdateTemplatePropertyAction {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
    ) : this(AbstractTemplatePropertyUpdater(literalService, resourceService, statementService))

    override fun invoke(command: UpdateTemplatePropertyCommand, state: State): State =
        state.apply {
            if (!command.matchesProperty(templateProperty!!)) {
                abstractTemplatePropertyUpdater.update(
                    statements = state.statements[templateProperty.id].orEmpty(),
                    contributorId = command.contributorId,
                    order = templateProperty.order.toInt(),
                    newProperty = command,
                    oldProperty = templateProperty
                )
            }
        }
}
