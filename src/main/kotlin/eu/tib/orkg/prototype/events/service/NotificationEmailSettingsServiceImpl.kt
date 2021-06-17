package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationEmailSettings
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.NotificationEmailSettingsRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class NotificationEmailSettingsServiceImpl(
    private val repository: NotificationEmailSettingsRepository
): NotificationEmailSettingsService {

    override fun addNotificationEmailSetting(notificationEmailSettingsDTO: NotificationEmailSettingsDTO) {
        val settings = repository.findByUserId(notificationEmailSettingsDTO.userId!!)
        if(settings.isEmpty){
            var newSettings = NotificationEmailSettings()
            newSettings.id = UUID.randomUUID()
            newSettings.userId = notificationEmailSettingsDTO.userId
            newSettings.timeOfPreference = notificationEmailSettingsDTO.time!!
            repository.save(newSettings)
        }
    }

    override fun updateNotificationEmailSetting(notificationEmailSettingsDTO: NotificationEmailSettingsDTO) {
        var settings = repository.findByUserId(notificationEmailSettingsDTO.userId!!)

        if(settings.isPresent){
            var settingsObject = settings.get()
            settingsObject.userId = notificationEmailSettingsDTO.userId
            settingsObject.timeOfPreference = notificationEmailSettingsDTO.time!!
            repository.save(settingsObject)
        }
    }

    override fun getNotificationEmailSettings(userId: UUID):
        NotificationEmailSettingsDTO {
        val settings = repository.findByUserId(userId)
        if(settings.isPresent){
            return NotificationEmailSettingsDTO(settings.get().userId,
                settings.get().timeOfPreference)
        }

        return NotificationEmailSettingsDTO()
    }
}
