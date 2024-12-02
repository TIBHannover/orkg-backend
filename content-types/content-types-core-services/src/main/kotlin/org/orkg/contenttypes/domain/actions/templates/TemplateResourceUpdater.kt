package org.orkg.contenttypes.domain.actions.templates

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class TemplateResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdateTemplateAction {
    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        resourceService.update(
            UpdateResourceUseCase.UpdateCommand(
                id = command.templateId,
                label = command.label,
                observatoryId = command.observatories?.ifEmpty { listOf(ObservatoryId.UNKNOWN) }?.singleOrNull(),
                organizationId = command.organizations?.ifEmpty { listOf(OrganizationId.UNKNOWN) }?.singleOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state
    }
}
