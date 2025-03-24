package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class RosettaStoneTemplateDescriptionUpdater(
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
) : UpdateRosettaStoneTemplateAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        if (command.description != state.rosettaStoneTemplate!!.description) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = state.statements[command.templateId].orEmpty(),
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.description,
                label = command.description
            )
        }
        return state
    }
}
