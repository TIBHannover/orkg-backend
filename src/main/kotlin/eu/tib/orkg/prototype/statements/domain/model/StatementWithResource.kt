package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.*

data class StatementWithResource(
    val id: Long,
    val subject: Resource,
    val predicate: Predicate,
    val `object`: ResourceObject
) : StatementResponse
