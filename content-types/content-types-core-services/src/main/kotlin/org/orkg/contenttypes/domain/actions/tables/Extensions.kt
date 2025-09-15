package org.orkg.contenttypes.domain.actions.tables

import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.input.RowCommand

internal fun Table.Row.toRowCommand(): RowCommand =
    RowCommand(
        label = label,
        data = data.map { it?.id?.value }
    )
