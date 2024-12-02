package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class RosettaStoneTemplateResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdateRosettaStoneTemplateAction {
    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        resourceService.update(
            UpdateResourceUseCase.UpdateCommand(
                id = command.templateId,
                label = command.label,
                observatoryId = command.observatories?.ifEmpty { listOf(ObservatoryId.UNKNOWN) }?.singleOrNull(),
                organizationId = command.organizations?.ifEmpty { listOf(OrganizationId.UNKNOWN) }?.singleOrNull()
            )
        )
        return state
    }
}
