package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplateTargetClassCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) : CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        unsafeStatementUseCases.create(
            CreateCommand(
                contributorId = command.contributorId,
                subjectId = state.templateId!!,
                predicateId = Predicates.shTargetClass,
                objectId = command.targetClass
            )
        )
        return state
    }
}
