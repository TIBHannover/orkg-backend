package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.application.StatementResponse
import java.time.OffsetDateTime

data class StatementWithLiteral(
    val id: StatementId,
    val subject: Resource,
    val predicate: Predicate,
    val `object`: LiteralObject,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime
) : StatementResponse
