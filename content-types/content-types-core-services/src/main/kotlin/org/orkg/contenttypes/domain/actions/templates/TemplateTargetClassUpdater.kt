package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplateTargetClassUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
) : UpdateTemplateAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementUseCases: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementUseCases, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        if (command.targetClass != null && command.targetClass != state.template!!.targetClass.id) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = state.statements[command.templateId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.shTargetClass,
                objectId = command.targetClass!!
            )
        }
        return state
    }
}
