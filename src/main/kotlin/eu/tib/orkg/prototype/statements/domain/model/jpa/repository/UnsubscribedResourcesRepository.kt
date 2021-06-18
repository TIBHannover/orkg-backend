package eu.tib.orkg.prototype.statements.domain.model.jpa.repository

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.UnsubscribedResources
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface UnsubscribedResourcesRepository:
    JpaRepository<UnsubscribedResources, UUID> {
    fun findByUserIdAndResourceId(userId: UUID, resourceId: String): Optional<UnsubscribedResources>
}
