package org.orkg.contenttypes.adapter.output.simcomp.internal

import java.time.LocalDateTime
import java.util.*

data class ThingGetResponse(
    val timestamp: LocalDateTime,
    val uuid: UUID,
    val payload: Payload
)
