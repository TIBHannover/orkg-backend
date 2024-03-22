package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class PaperWithStatementCount(
    val id: ThingId,
    val title: String,
    val count: Long
)
