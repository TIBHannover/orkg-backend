package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.UnsubscribedResources
import java.util.UUID

interface UnsubscribedResourcesService {

    fun addResourceAsUnsubscribed(userId: UUID, resourceId: String): UnsubscribedResources

    fun removeResourceAsUnsubscribed(userId: UUID, resourceId: String)

    fun getResourceAsUnsubscribed(userId: UUID, resourceId: String): Boolean
}
