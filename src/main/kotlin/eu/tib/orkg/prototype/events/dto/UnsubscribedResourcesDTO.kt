package eu.tib.orkg.prototype.events.dto

import java.util.UUID

data class UnsubscribedResourcesDTO(
    val id: UUID? = null,
    val userId: UUID,
    val resourceId: String
)
