package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.StatementUseCases

class TemplateTargetClassCreator(
    private val statementUseCases: StatementUseCases
) : CreateTemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State =
        state.apply {
            statementUseCases.add(
                CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = templateId!!,
                    predicateId = Predicates.shTargetClass,
                    objectId = command.targetClass
                )
            )
        }
}
