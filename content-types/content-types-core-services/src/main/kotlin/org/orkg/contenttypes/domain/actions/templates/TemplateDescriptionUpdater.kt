package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class TemplateDescriptionUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater
) : UpdateTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(SingleStatementPropertyUpdater(literalService, statementService))

    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        if (command.description != state.template!!.description) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.description,
                label = command.description
            )
        }
        return state
    }
}
