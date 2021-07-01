package eu.tib.orkg.prototype.statements.domain.model.jpa.repository

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationEmailSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface NotificationEmailSettingsRepository: JpaRepository<NotificationEmailSettings, UUID> {
    fun findByUserId(userId: UUID): Optional<NotificationEmailSettings>
    fun findAllByTimeOfPreferenceEquals(time: Int): List<NotificationEmailSettings>
}
