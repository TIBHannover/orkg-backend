package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class ComparisonResourceUpdater(
    private val resourceService: ResourceUseCases
) : UpdateComparisonAction {
    override operator fun invoke(command: UpdateComparisonCommand, state: State): State =
        state.apply {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = command.comparisonId,
                    label = command.title,
                    observatoryId = command.observatories?.singleOrNull(),
                    organizationId = command.organizations?.singleOrNull(),
                    extractionMethod = command.extractionMethod
                )
            )
        }
}
