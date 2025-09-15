package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.TableService
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.output.ResourceRepository

class TableExistenceValidator<T, S>(
    private val tableService: TableService,
    private val resourceRepository: ResourceRepository,
    private val tableIdSelector: (T) -> ThingId,
    private val stateUpdater: S.(Table, Map<ThingId, List<GeneralStatement>>) -> S,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val tableId = tableIdSelector(command)
        val resource = resourceRepository.findById(tableId)
            .orElseThrow { TableNotFound(tableId) }
        val subgraph = tableService.findSubgraph(resource)
        val table = Table.from(resource, subgraph.statements)
        return stateUpdater(state, table, subgraph.statements)
    }
}
