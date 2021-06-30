package eu.tib.orkg.prototype.statements.domain.model.jpa.repository

import eu.tib.orkg.prototype.events.service.NotificationAnalytics
import eu.tib.orkg.prototype.events.service.NotificationAnalyticsByUser
import eu.tib.orkg.prototype.events.service.NotificationAnalyticsByUser2
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationUpdates
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificationUpdatesRepository: JpaRepository<NotificationUpdates, UUID> {
    fun findAllByUserId(userId: UUID): List<NotificationUpdates>
    fun findAllByNotificationByUserIDNot(userId: UUID): List<NotificationUpdates>

    @Query(value="Select resource_type, COUNT(resource_type) from notification_updates AS \"n_updates\" WHERE \"n_updates\".user_id=?1 AND \"n_updates\".created_date_time BETWEEN now() - INTERVAL '24 HOURS' AND now() GROUP BY resource_type",
    nativeQuery=true)
    fun getDailyStatisticsByUser(userId: UUID): List<NotificationAnalytics>

    @Modifying
    @Query(value="Select resource_type as resourceType, COUNT(resource_type), CAST(user_id as varchar) user_id as userId from notification_updates GROUP BY user_id, resource_type",
        nativeQuery = true)
    fun getDailyStatisticsOfAllUsers(): List<NotificationAnalyticsByUser2>

}
