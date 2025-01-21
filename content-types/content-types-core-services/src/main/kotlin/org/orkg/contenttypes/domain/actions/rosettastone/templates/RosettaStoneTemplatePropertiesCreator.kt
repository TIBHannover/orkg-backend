package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class RosettaStoneTemplatePropertiesCreator(
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator
) : CreateRosettaStoneTemplateAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(AbstractTemplatePropertyCreator(unsafeResourceUseCases, literalService, statementService))

    override fun invoke(
        command: CreateRosettaStoneTemplateCommand,
        state: CreateRosettaStoneTemplateState
    ): CreateRosettaStoneTemplateState {
        command.properties.forEachIndexed { index, property ->
            abstractTemplatePropertyCreator.create(command.contributorId, state.rosettaStoneTemplateId!!, index, property)
        }
        return state
    }
}
