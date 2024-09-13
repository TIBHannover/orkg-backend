package org.orkg.contenttypes.adapter.output.simcomp.internal

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.*
import org.orkg.common.ThingId

data class BaseThing(
    val id: UUID,
    @JsonProperty("created_at")
    val createdAt: LocalDateTime,
    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime,
    @JsonProperty("thing_type")
    val thingType: ThingType,
    @JsonProperty("thing_key")
    val thingKey: ThingId,
    val config: JsonNode,
    val data: JsonNode
)
