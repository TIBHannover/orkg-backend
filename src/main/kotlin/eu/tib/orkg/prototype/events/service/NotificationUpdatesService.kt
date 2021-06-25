package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationUpdates
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.security.Principal
import java.util.UUID

interface NotificationUpdatesService {
    fun addNotificationUpdate(notification: NotificationUpdates)
    fun retrieveNotificationUpdates(userId: UUID): Iterable<NotificationUpdatesWithProfile>
    fun deleteNotificationById(id: UUID)
}
