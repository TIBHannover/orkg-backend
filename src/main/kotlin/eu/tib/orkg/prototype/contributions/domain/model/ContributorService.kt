package eu.tib.orkg.prototype.contributions.domain.model

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class ContributorService(
    private val userRepository: UserRepository
) {
    fun findById(userId: UUID): Optional<Contributor> =
        userRepository
            .findById(userId)
            .map(UserEntity::toContributor)

    /**
     * Attempt to find a contributor with a given ID, or return a default user.
     */
    fun findByIdOrElseUnknown(userId: UUID): Contributor =
        findById(userId)
            .orElse(
                Contributor(
                    id = UUID(0, 0),
                    name = "Unknown User",
                    joinedAt = OffsetDateTime.MIN
                )
            )

    fun findOrganizationById(userId: UUID): Optional<Contributor> =
        userRepository
            .findOrganizationById(userId)
            .map(UserEntity::toContributor)

    fun findUsersByOrganizationId(id: UUID): Iterable<Contributor> =
        userRepository
            .findUsersByOrganizationId(id)
            .map(UserEntity::toContributor)
}
