package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class TemplateTargetClassUpdater(
    private val statementUseCases: StatementUseCases
) : UpdateTemplateAction {
    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        if (command.targetClass != null && command.targetClass != state.template!!.targetClass.id) {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.shTargetClass,
                objectClasses = setOf(Classes.`class`),
                pageable = PageRequests.ALL
            ).forEach {
                statementUseCases.delete(it.id)
            }

            statementUseCases.add(
                userId = command.contributorId,
                subject = command.templateId,
                predicate = Predicates.shTargetClass,
                `object` = command.targetClass!!
            )
        }
        return state
    }
}
