package eu.tib.orkg.prototype.contributions.domain.model

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.application.ports.input.RetrieveContributorUseCase
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import java.time.OffsetDateTime
import java.util.Optional
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class ContributorService(
    private val userRepository: UserRepository
) : RetrieveContributorUseCase {
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

    fun findUsersByOrganizationId(id: OrganizationId): Iterable<Contributor> =
        userRepository
            .findByOrganizationId(id.value)
            .map(UserEntity::toContributor)

    fun findUsersByObservatoryId(id: ObservatoryId): Iterable<Contributor> =
        userRepository
            .findByObservatoryId(id.value)
            .map(UserEntity::toContributor)

    override fun byId(id: ContributorId): Optional<Contributor> = findById(id)
}
