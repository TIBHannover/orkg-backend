package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId

data class CreateStatement(
    val id: StatementId? = null,
    @JsonProperty("subject_id")
    val subjectId: ThingId,
    @JsonProperty("predicate_id")
    val predicateId: ThingId,
    @JsonProperty("object_id")
    val objectId: ThingId
)
