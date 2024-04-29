package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId
import org.orkg.graph.domain.StatementId

data class StatementEditRequest(
    @JsonProperty("statement_id")
    val statementId: StatementId?,

    @JsonProperty("subject_id")
    val subjectId: ThingId?,

    @JsonProperty("predicate_id")
    val predicateId: ThingId?,

    @JsonProperty("object_id")
    val objectId: ThingId?
)
