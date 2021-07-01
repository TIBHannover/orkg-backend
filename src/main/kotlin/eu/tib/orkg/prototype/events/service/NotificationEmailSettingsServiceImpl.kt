package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationEmailSettings
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.NotificationEmailSettingsRepository
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.logging.Logger


@Service
class NotificationEmailSettingsServiceImpl(
    private val repository: NotificationEmailSettingsRepository
): NotificationEmailSettingsService {

    private val logger = Logger.getLogger("Notification Email")
    override fun addNotificationEmailSetting(notificationEmailSettingsDTO: NotificationEmailSettingsDTO) {
            var newSettings = NotificationEmailSettings()
            newSettings.id = UUID.randomUUID()
            newSettings.userId = notificationEmailSettingsDTO.userId
            newSettings.timeOfPreference = notificationEmailSettingsDTO.time!!
            repository.save(newSettings)
    }

    override fun updateNotificationEmailSetting(notificationEmailSettingsDTO: NotificationEmailSettingsDTO) {
        var settings = repository.findByUserId(notificationEmailSettingsDTO.userId!!)

        if(settings.isEmpty){
            addNotificationEmailSetting(notificationEmailSettingsDTO)
        }else {
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

    override fun getAllEmailSubscribedUsers(): List<UUID> {
        val subscribedUsers = repository.findAllByTimeOfPreferenceEquals(18)
        val listOfUsers = mutableListOf<UUID>()
        subscribedUsers.map {
            if(it.userId != null){
                listOfUsers.add(it.userId!!)
            }
        }

        return listOfUsers

    }
}
