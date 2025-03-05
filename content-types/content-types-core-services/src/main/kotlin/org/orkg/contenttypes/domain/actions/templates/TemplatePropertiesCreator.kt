package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplatePropertiesCreator(
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator,
) : CreateTemplateAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(AbstractTemplatePropertyCreator(unsafeResourceUseCases, unsafeLiteralUseCases, unsafeStatementUseCases))

    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.properties.forEachIndexed { index, property ->
            abstractTemplatePropertyCreator.create(command.contributorId, state.templateId!!, index, property)
        }
        return state
    }
}
