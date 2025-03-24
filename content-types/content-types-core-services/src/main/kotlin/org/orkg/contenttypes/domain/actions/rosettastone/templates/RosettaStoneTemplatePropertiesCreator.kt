package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyCreator
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.CreateRosettaStoneTemplateAction.State
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class RosettaStoneTemplatePropertiesCreator(
    private val abstractTemplatePropertyCreator: AbstractTemplatePropertyCreator,
) : CreateRosettaStoneTemplateAction {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractTemplatePropertyCreator(unsafeResourceUseCases, unsafeLiteralUseCases, unsafeStatementUseCases)
    )

    override fun invoke(command: CreateRosettaStoneTemplateCommand, state: State): State {
        command.properties.forEachIndexed { index, property ->
            abstractTemplatePropertyCreator.create(command.contributorId, state.rosettaStoneTemplateId!!, index, property)
        }
        return state
    }
}
