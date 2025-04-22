package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class TableResourceUpdater(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : UpdateTableAction {
    override fun invoke(command: UpdateTableCommand, state: State): State {
        unsafeResourceUseCases.update(
            UpdateResourceUseCase.UpdateCommand(
                id = command.tableId,
                contributorId = command.contributorId,
                label = command.label,
                observatoryId = command.observatories?.ifEmpty { listOf(ObservatoryId.UNKNOWN) }?.singleOrNull(),
                organizationId = command.organizations?.ifEmpty { listOf(OrganizationId.UNKNOWN) }?.singleOrNull(),
                extractionMethod = command.extractionMethod,
                visibility = command.visibility
            )
        )
        return state
    }
}
