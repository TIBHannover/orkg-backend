package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.StatementResponse
import java.time.LocalDateTime

data class StatementWithLiteral(
    val id: StatementId,
    val subject: Resource,
    val predicate: Predicate,
    val `object`: LiteralObject,
    val created: LocalDateTime = LocalDateTime.now()
) : StatementResponse
