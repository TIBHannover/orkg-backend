package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertiesUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplatePropertiesUpdater(
    private val abstractTemplatePropertiesUpdater: AbstractTemplatePropertiesUpdater,
) : UpdateTemplateAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        resourceService: ResourceUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractTemplatePropertiesUpdater(
            unsafeLiteralUseCases,
            resourceService,
            unsafeResourceUseCases,
            statementService,
            unsafeStatementUseCases
        )
    )

    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        command.properties?.also { properties ->
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
