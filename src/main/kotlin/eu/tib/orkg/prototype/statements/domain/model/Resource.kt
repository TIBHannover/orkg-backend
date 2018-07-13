package eu.tib.orkg.prototype.statements.domain.model

import java.time.LocalDateTime

data class Resource(
    val id: ResourceId?,
    val label: String
) {
    val created: LocalDateTime = LocalDateTime.now()
}
