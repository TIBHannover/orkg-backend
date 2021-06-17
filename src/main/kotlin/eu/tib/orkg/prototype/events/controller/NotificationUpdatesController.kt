package eu.tib.orkg.prototype.events.controller

import eu.tib.orkg.prototype.events.service.NotificationEmailSettingsDTO
import eu.tib.orkg.prototype.events.service.NotificationEmailSettingsService
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationUpdates
import eu.tib.orkg.prototype.events.service.NotificationUpdatesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class NotificationUpdatesController(
private val notificationUpdatesService: NotificationUpdatesService,
private val notificationEmailSettingsService: NotificationEmailSettingsService
){
    @GetMapping("/{userId}")
    fun getNotifications(@PathVariable userId: UUID, pageable: Pageable): Page<NotificationUpdates> =
        notificationUpdatesService.retrieveNotificationUpdates(userId, pageable)

    @DeleteMapping("/{id}")
    fun deleteNotificationById(@PathVariable id: UUID) =
        notificationUpdatesService.deleteNotificationById(id)

    @GetMapping("/email/user/{userId}")
    fun getNotificationEmailSettings(@PathVariable userId: UUID) =
        notificationEmailSettingsService.getNotificationEmailSettings(userId)

    @PostMapping("/email")
    fun saveNotificationEmailSettings(@RequestBody notificationEmailSettingsDTO: NotificationEmailSettingsDTO) =
        notificationEmailSettingsService.updateNotificationEmailSetting(notificationEmailSettingsDTO)
}
