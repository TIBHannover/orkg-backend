package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.StatementId

/**
 * The row subgraph is defined as the row resource with all incoming and outgoing statements.
 * The statement that links to each cell resource is not included in statementIds.
 */
data class RowGraph(
    val tableHasRowStatement: GeneralStatement,
    val labelStatement: GeneralStatement?,
    val indexStatement: GeneralStatement?,
    val cells: List<CellGraph?>,
) {
    val rowId: ThingId = tableHasRowStatement.`object`.id
    val label: String? = labelStatement?.`object`?.label
    val statementIds: Set<StatementId> = setOfNotNull(
        tableHasRowStatement.id,
        labelStatement?.id,
        indexStatement?.id,
    )

    fun toRow(): Table.Row =
        Table.Row(label, cells.map { it?.value })
}
