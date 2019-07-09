package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.LiteralId

data class StatementWithLiteralRequest(
    val `object`: LiteralObjectRequest
)

data class LiteralObjectRequest(
    val id: LiteralId
)
