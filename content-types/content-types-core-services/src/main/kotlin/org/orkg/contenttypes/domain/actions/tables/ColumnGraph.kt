package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.StatementId

/**
 * The column subgraph is defined as the column resource with all incoming and outgoing statements,
 * expect for incoming statements from cell resources.
 */
data class ColumnGraph(
    val tableHasColumnStatement: GeneralStatement,
    val labelStatement: GeneralStatement?,
    val indexStatement: GeneralStatement?,
) {
    val columnId: ThingId = tableHasColumnStatement.`object`.id
    val label: String? = labelStatement?.`object`?.label
    val statementIds: Set<StatementId> = setOfNotNull(
        tableHasColumnStatement.id,
        labelStatement?.id,
        indexStatement?.id,
    )
}
