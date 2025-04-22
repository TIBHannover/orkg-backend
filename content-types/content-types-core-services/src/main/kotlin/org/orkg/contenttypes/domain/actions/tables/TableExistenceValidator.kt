package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.TableService
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.graph.output.ResourceRepository

class TableExistenceValidator(
    private val tableService: TableService,
    private val resourceRepository: ResourceRepository,
) : UpdateTableAction {
    override fun invoke(command: UpdateTableCommand, state: State): State {
        val resource = resourceRepository.findById(command.tableId)
            .orElseThrow { TableNotFound(command.tableId) }
        val subgraph = tableService.findSubgraph(resource)
        val table = Table.from(resource, subgraph.statements)
        return state.copy(table = table, statements = subgraph.statements)
    }
}
