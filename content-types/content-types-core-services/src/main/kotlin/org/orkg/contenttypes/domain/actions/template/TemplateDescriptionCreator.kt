package org.orkg.contenttypes.domain.actions.template

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.DescriptionCreator
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class TemplateDescriptionCreator(
    literalService: LiteralUseCases,
    statementService: StatementUseCases
) : DescriptionCreator(literalService, statementService), TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State =
        state.apply { command.description?.let { create(command.contributorId, state.templateId!!, it) } }
}
