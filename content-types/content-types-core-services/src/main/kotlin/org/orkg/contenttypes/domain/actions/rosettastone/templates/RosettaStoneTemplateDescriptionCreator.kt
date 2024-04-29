package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class RosettaStoneTemplateDescriptionCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator
) : CreateRosettaStoneTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(SingleStatementPropertyCreator(literalService, statementService))

    override fun invoke(
        command: CreateRosettaStoneTemplateCommand,
        state: CreateRosettaStoneTemplateState
    ): CreateRosettaStoneTemplateState =
        state.apply {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = rosettaStoneTemplateId!!,
                predicateId = Predicates.description,
                label = command.description
            )
        }
}
