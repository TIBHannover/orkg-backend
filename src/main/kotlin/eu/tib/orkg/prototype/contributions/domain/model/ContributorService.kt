package eu.tib.orkg.prototype.contributions.domain.model

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import java.time.OffsetDateTime
import java.util.Optional
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class ContributorService(
    private val userRepository: UserRepository
) {
    fun findById(userId: ContributorId): Optional<Contributor> =
        userRepository
            .findById(userId.value)
            .map(UserEntity::toContributor)

    /**
     * Attempt to find a contributor with a given ID, or return a default user.
     */
    fun findByIdOrElseUnknown(userId: ContributorId): Contributor =
        findById(userId)
            .orElse(
                Contributor(
                    id = ContributorId.createUnknownContributor(),
                    name = "Unknown User",
                    joinedAt = OffsetDateTime.MIN
                )
            )

    fun findOrganizationById(userId: ContributorId): Optional<Contributor> =
        userRepository
            .findOrganizationById(userId.value)
            .map(UserEntity::toContributor)

    fun findUsersByOrganizationId(id: ContributorId): Iterable<Contributor> =
        userRepository
            .findUsersByOrganizationId(id.value)
            .map(UserEntity::toContributor)

    fun findUsersByObservatoryId(id: ContributorId): Iterable<Contributor> =
        userRepository
            .findUsersByObservatoryId(id.value)
            .map(UserEntity::toContributor)
}
