package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.*

data class StatementWithLiteral(
    val id: Long,
    val subject: Resource,
    val predicate: Predicate,
    val `object`: LiteralObject
) : StatementResponse
