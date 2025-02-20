package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class ObjectIdAndLabel(
    val id: ThingId,
    val label: String,
)
