package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.application.StatementResponse
import java.time.OffsetDateTime
import java.util.UUID

data class GeneralStatement(
    val id: StatementId? = null,
    val subject: Thing,
    val predicate: Predicate,
    val `object`: Thing,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: UUID = UUID(0, 0)

) : StatementResponse

data class CreateStatement(
    val id: StatementId? = null,
    @JsonProperty("subject_id")
    val subjectId: String,
    @JsonProperty("predicate_id")
    val predicateId: PredicateId,
    @JsonProperty("object_id")
    val objectId: String
)
