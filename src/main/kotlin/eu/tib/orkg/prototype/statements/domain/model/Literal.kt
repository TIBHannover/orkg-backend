package eu.tib.orkg.prototype.statements.domain.model

import java.time.OffsetDateTime

data class Literal(
    val id: LiteralId?,
    val label: String,
    val createdAt: OffsetDateTime?
)
