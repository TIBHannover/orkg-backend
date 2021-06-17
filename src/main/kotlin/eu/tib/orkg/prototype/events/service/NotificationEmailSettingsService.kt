package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationEmailSettings
import java.util.UUID

interface NotificationEmailSettingsService {
    fun addNotificationEmailSetting(
        notificationEmailSettingsDTO: NotificationEmailSettingsDTO)

    fun updateNotificationEmailSetting(
        notificationEmailSettingsDTO: NotificationEmailSettingsDTO)

    fun getNotificationEmailSettings(userId: UUID): NotificationEmailSettingsDTO
}

data class NotificationEmailSettingsDTO(
    val userId: UUID? = null,
    val time: Int? = -1
)
