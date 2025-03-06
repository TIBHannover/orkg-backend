package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.actions.CreateTableCommand
import org.orkg.contenttypes.domain.actions.tables.CreateTableAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class TableResourceCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : CreateTableAction {
    override fun invoke(command: CreateTableCommand, state: State): State {
        val tableId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = command.label,
                classes = setOf(Classes.table),
                contributorId = command.contributorId,
                observatoryId = command.observatories.firstOrNull(),
                organizationId = command.organizations.firstOrNull(),
                extractionMethod = command.extractionMethod
            )
        )
        return state.copy(tableId = tableId)
    }
}
