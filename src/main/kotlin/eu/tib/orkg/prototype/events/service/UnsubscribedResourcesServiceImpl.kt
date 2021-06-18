package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.UnsubscribedResources
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.UnsubscribedResourcesRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UnsubscribedResourcesServiceImpl(
    private val repository: UnsubscribedResourcesRepository
): UnsubscribedResourcesService {
    override fun addResourceAsUnsubscribed(userId: UUID, resourceId: String): UnsubscribedResources {
        val unsubscribeObject = repository.findByUserIdAndResourceId(userId, resourceId)

        if(unsubscribeObject.isEmpty) {
            var unsubscribe = UnsubscribedResources()
            unsubscribe.id = UUID.randomUUID()
            unsubscribe.userId = userId
            unsubscribe.resourceId = resourceId

            return repository.save(unsubscribe)
        }
        return unsubscribeObject.get()
    }

    override fun removeResourceAsUnsubscribed(userId: UUID, resourceId: String) {
        val unsubscribeObject = repository.findByUserIdAndResourceId(userId, resourceId)
        if(unsubscribeObject.isPresent) {
            repository.delete(unsubscribeObject.get())
        }
    }

    override fun getResourceAsUnsubscribed(userId: UUID, resourceId: String): Boolean =
        repository.findByUserIdAndResourceId(userId, resourceId).isPresent
}
