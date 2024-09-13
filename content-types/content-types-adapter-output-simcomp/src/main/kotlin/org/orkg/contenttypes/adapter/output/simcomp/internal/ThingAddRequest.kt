package org.orkg.contenttypes.adapter.output.simcomp.internal

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId

data class ThingAddRequest(
    @JsonProperty("thing_type")
    val thingType: ThingType,
    @JsonProperty("thing_key")
    val thingKey: ThingId,
    val config: Any,
    val data: Any
)
