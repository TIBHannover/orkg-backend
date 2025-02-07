package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class RosettaStoneTemplateFormattedLabelUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater
) : UpdateRosettaStoneTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases
    ) : this(SingleStatementPropertyUpdater(literalService, statementService, unsafeStatementUseCases))

    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        if (command.formattedLabel != state.rosettaStoneTemplate!!.formattedLabel) {
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
