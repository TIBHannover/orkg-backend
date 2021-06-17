package eu.tib.orkg.prototype.statements.domain.model.jpa.repository

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationUpdates
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificationUpdatesRepository: JpaRepository<NotificationUpdates, UUID> {
    fun findAllByUserId(userId: UUID, pageable: Pageable): Page<NotificationUpdates>
    fun findAllByNotificationByUserIDNot(userId: UUID): List<NotificationUpdates>
}
