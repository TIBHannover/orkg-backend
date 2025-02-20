package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.CreateTemplatePropertyAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplatePropertyCreator(
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator,
) : CreateTemplatePropertyAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        literalService: LiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(AbstractTemplatePropertyCreator(unsafeResourceUseCases, literalService, unsafeStatementUseCases))

    override fun invoke(command: CreateTemplatePropertyCommand, state: State): State = state.copy(
        templatePropertyId = abstractTemplatePropertyCreator.create(
            contributorId = command.contributorId,
            templateId = command.templateId,
            order = state.propertyCount!!,
            property = command
        ),
        propertyCount = state.propertyCount + 1
    )
}
