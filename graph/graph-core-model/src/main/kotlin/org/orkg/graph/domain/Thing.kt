package org.orkg.graph.domain

import org.orkg.common.ThingId

sealed interface Thing {
    val id: ThingId
    val label: String
}
