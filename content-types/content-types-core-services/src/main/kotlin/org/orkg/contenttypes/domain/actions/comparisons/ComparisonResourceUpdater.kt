package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class ComparisonResourceUpdater(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : UpdateComparisonAction {
    override fun invoke(command: UpdateComparisonCommand, state: State): State {
        unsafeResourceUseCases.update(
            UpdateResourceUseCase.UpdateCommand(
                id = command.comparisonId,
                contributorId = command.contributorId,
                label = command.title,
                classes = command.type?.let { type -> setOfNotNull(Classes.comparison, type.`class`) },
                observatoryId = command.observatories?.ifEmpty { listOf(ObservatoryId.UNKNOWN) }?.singleOrNull(),
                organizationId = command.organizations?.ifEmpty { listOf(OrganizationId.UNKNOWN) }?.singleOrNull(),
                extractionMethod = command.extractionMethod,
                visibility = command.visibility,
            ),
        )
        return state
    }
}
