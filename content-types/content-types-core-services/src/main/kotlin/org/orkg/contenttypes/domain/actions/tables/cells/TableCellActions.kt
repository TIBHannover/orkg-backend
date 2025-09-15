@file:Suppress("ktlint:standard:filename")

package org.orkg.contenttypes.domain.actions.tables.cells

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.UpdateTableCellCommand
import org.orkg.graph.domain.GeneralStatement

interface UpdateTableCellAction : Action<UpdateTableCellCommand, UpdateTableCellAction.State> {
    data class State(
        val table: Table? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}
