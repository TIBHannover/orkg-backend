package eu.tib.orkg.prototype.statements.domain.model

import java.time.OffsetDateTime

data class Resource(
    val id: ResourceId?,
    val label: String,
    val createdAt: OffsetDateTime?,
    val classes: Set<ClassId> = emptySet()
)
