package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplateFormattedLabelUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
) : UpdateTemplateAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        if (command.formattedLabel != state.template!!.formattedLabel) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = state.statements[command.templateId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.templateLabelFormat,
                label = command.formattedLabel?.value
            )
        }
        return state
    }
}
