package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class RosettaStoneTemplateDescriptionCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
) : CreateRosettaStoneTemplateAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(SingleStatementPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases))

    override fun invoke(
        command: CreateRosettaStoneTemplateCommand,
        state: CreateRosettaStoneTemplateState,
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
