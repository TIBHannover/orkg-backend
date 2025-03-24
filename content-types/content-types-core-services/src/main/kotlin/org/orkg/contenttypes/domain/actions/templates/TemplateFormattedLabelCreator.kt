package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplateFormattedLabelCreator(
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) : CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.formattedLabel?.let { label ->
            val labelLiteralId = unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = label.value
                )
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.templateLabelFormat,
                    objectId = labelLiteralId
                )
            )
        }
        return state
    }
}
