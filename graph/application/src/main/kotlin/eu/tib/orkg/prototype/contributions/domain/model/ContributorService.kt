package eu.tib.orkg.prototype.contributions.domain.model

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.UserEntity
import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaUserRepository
import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.toContributor
import eu.tib.orkg.prototype.contributions.application.ports.input.RetrieveContributorUseCase
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import java.time.OffsetDateTime
import java.util.Optional
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class ContributorService(
    private val userRepository: JpaUserRepository
) : RetrieveContributorUseCase {
    fun findById(userId: ContributorId): Optional<Contributor> =
        userRepository
            .findById(userId.value)
            .map(UserEntity::toUser)
            .map(User::toContributor)

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

    fun findUsersByOrganizationId(id: OrganizationId): Iterable<Contributor> =
        userRepository
            .findByOrganizationId(id.value)
            .map(UserEntity::toUser)
            .map(User::toContributor)

    fun findUsersByObservatoryId(id: ObservatoryId): Iterable<Contributor> =
        userRepository
            .findByObservatoryId(id.value)
            .map(UserEntity::toUser)
            .map(User::toContributor)

    override fun byId(id: ContributorId): Optional<Contributor> = findById(id)
}
