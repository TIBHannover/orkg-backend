package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.TemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class TemplateFormattedLabelCreator(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) : TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.formattedLabel?.let { label ->
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.templateLabelFormat,
                `object` = literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = command.contributorId,
                        label = label.value
                    )
                )
            )
        }
        return state
    }
}
