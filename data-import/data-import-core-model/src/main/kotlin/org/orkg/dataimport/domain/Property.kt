package org.orkg.dataimport.domain

import org.orkg.common.ThingId

data class Property(
    val name: String,
    val type: ThingId,
    val validator: ((String) -> Unit)? = null,
)
