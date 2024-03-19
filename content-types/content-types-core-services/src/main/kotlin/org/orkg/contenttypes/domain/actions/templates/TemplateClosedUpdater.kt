package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class TemplateClosedUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater
) : UpdateTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(SingleStatementPropertyUpdater(literalService, statementService))

    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        if (command.isClosed != null && command.isClosed != state.template!!.isClosed) {
            singleStatementPropertyUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.shClosed,
                label = command.isClosed!!.toString(),
                datatype = Literals.XSD.BOOLEAN.prefixedUri
            )
        }
        return state
    }
}
