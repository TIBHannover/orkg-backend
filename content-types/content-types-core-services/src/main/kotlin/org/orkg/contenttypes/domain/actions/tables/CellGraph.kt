package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Thing

/**
 * The cell subgraph is defined as the cell resource with all incoming and outgoing statements.
 * The value statement also has to be included in statementIds.
 */
data class CellGraph(
    val rowHasCellStatement: GeneralStatement,
    val valueStatement: GeneralStatement?,
    val columnStatement: GeneralStatement?,
) {
    val cellId: ThingId = rowHasCellStatement.`object`.id
    val value: Thing? = valueStatement?.`object`
    val statementIds = setOfNotNull(
        rowHasCellStatement.id,
        columnStatement?.id,
        valueStatement?.id
    )
}
