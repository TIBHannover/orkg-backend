package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.CreateRosettaStoneTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class RosettaStoneTemplateResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : CreateRosettaStoneTemplateAction {
    override fun invoke(command: CreateRosettaStoneTemplateCommand, state: State): State {
        val rosettaStoneTemplateId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.label,
                classes = setOf(Classes.rosettaNodeShape),
                observatoryId = command.observatories.firstOrNull(),
                organizationId = command.organizations.firstOrNull()
            )
        )
        return state.copy(rosettaStoneTemplateId = rosettaStoneTemplateId)
    }
}
