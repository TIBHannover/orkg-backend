package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.StatementResponse
import java.time.OffsetDateTime

data class StatementWithResource(
    val id: StatementId,
    val subject: Resource,
    val predicate: Predicate,
    val `object`: ResourceObject,
    val createdAt: OffsetDateTime
) : StatementResponse
