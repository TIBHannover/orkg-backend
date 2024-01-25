package org.orkg.contenttypes.domain.actions.template

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class TemplateTargetClassCreator(
    private val statementUseCases: StatementUseCases
) : TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State =
        state.apply {
            statementUseCases.add(
                userId = command.contributorId,
                subject = templateId!!,
                predicate = Predicates.shTargetClass,
                `object` = command.targetClass
            )
        }
}
