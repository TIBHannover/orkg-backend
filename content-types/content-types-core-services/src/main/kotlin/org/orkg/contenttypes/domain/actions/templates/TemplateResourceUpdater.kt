package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class TemplateResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdateTemplateAction {
    override operator fun invoke(command: UpdateTemplateCommand, state: UpdateTemplateState): UpdateTemplateState =
        state.apply {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = command.templateId,
                    label = command.label,
                    observatoryId = command.observatories?.singleOrNull(),
                    organizationId = command.organizations?.singleOrNull(),
                    extractionMethod = command.extractionMethod
                )
            )
        }
}
