package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationUpdates
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.NotificationUpdatesRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.ResearchFieldsTreeRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.logging.Logger

@Service
class NotificationUpdatesServiceImpl(
    private val notificationUpdatesRepository: NotificationUpdatesRepository,
    private val researchFieldsTreeRepository: ResearchFieldsTreeRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,)
    : NotificationUpdatesService {

    private val logger = Logger.getLogger("Notification Updates")

    override fun addNotificationUpdate(notification: NotificationUpdates) {
        notificationUpdatesRepository.save(notification)
    }

    override fun retrieveNotificationUpdates(userId: UUID, pageable: Pageable):
        Page<NotificationUpdates> = notificationUpdatesRepository.findAllByUserId(userId, pageable)

    override fun deleteNotificationById(id: UUID) = notificationUpdatesRepository.deleteById(id)

}
