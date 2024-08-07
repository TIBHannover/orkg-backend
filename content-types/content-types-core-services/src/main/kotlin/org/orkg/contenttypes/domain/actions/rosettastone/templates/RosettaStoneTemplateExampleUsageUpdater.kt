package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class RosettaStoneTemplateExampleUsageUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater
) : UpdateRosettaStoneTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(SingleStatementPropertyUpdater(literalService, statementService))

    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        if (command.exampleUsage != null && command.exampleUsage != state.rosettaStoneTemplate!!.exampleUsage) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = state.statements[state.rosettaStoneTemplate.targetClass].orEmpty(),
                contributorId = command.contributorId,
                subjectId = state.rosettaStoneTemplate.targetClass,
                predicateId = Predicates.exampleOfUsage,
                label = command.exampleUsage
            )
        }
        return state
    }
}
