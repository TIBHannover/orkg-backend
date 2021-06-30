package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationUpdates
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.NotificationUpdatesRepository
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class NotificationUpdatesServiceImpl(
    private val notificationUpdatesRepository: NotificationUpdatesRepository,
    private val userRepository: UserRepository)
    : NotificationUpdatesService {

    override fun addNotificationUpdate(notification: NotificationUpdates) {
        notificationUpdatesRepository.save(notification)
    }

    override fun retrieveNotificationUpdates(userId: UUID): Iterable<NotificationUpdatesWithProfile> {
        val updates = notificationUpdatesRepository.findAllByUserId(userId)
        val distinctUserIDs = updates.map {
            it.notificationByUserID!!
        }.toTypedArray()

        return getContributorsWithProfile(updates, distinctUserIDs)
    }

    override fun deleteNotificationById(id: UUID) = notificationUpdatesRepository.deleteById(id)

    override fun getTotalResourcesByGroup(userId: UUID): List<NotificationAnalytics> =
        notificationUpdatesRepository.getDailyStatisticsByUser(userId)

    override fun getTotalResourcesByGroupOfAllUsers(): List<NotificationAnalyticsByUser2> =
        notificationUpdatesRepository.getDailyStatisticsOfAllUsers()

    private fun getContributorsWithProfile(updates: List<NotificationUpdates>, userIdList: Array<UUID>):
        Iterable<NotificationUpdatesWithProfile> {

        val mapValues = userRepository.findByIdIn(userIdList).map(UserEntity::toContributor).groupBy(Contributor::id)

        val refinedTopNotifications =
            updates.map { update ->
                val contributor = mapValues[ContributorId(update.notificationByUserID!!)]?.first()
                NotificationUpdatesWithProfile(update.id,
                    update.researchFieldTreeId,
                    update.userId,
                    Profile(contributor?.id,
                        contributor?.name,
                        contributor?.gravatarId,
                        contributor?.avatarURL),
                    update.resourceId,
                    update.resourceType,
                    update.title,
                    update.newPaper,
                    update.createdDateTime)
            } as MutableList<NotificationUpdatesWithProfile>

        return refinedTopNotifications
    }
}

data class NotificationUpdatesWithProfile(
    var id: UUID? = null,
    var researchFieldTreeId: UUID? = null,
    var userId: UUID? = null,
    var profile: Profile,
    var resourceId: String? = null,
    var resourceType: String? = null,
    var title: String? = null,
    var newPaper: Boolean = false,
    var createdDateTime: LocalDateTime = LocalDateTime.now()
)

data class NotificationAnalyticsByUser(
    val resource_type: String,
    val count: Int,
    val userId: String
)

interface NotificationAnalyticsByUser2{
    fun getResourceType(): String
    fun getCount(): Int
    fun getUserId(): String
}

data class NotificationAnalytics(
    val resource_type: String,
    val count: Int
)


