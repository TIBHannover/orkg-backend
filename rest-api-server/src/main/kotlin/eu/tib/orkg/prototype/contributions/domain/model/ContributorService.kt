package eu.tib.orkg.prototype.contributions.domain.model

import eu.tib.orkg.prototype.auth.persistence.ORKGUserEntity
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.OrkgUserRepository
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.application.ports.input.RetrieveContributorUseCase
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.ports.ContributorRepository
import java.time.OffsetDateTime
import java.util.Optional
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class ContributorService(
    private val userRepository: UserRepository,
    private val orkgUserRepository: OrkgUserRepository
) : RetrieveContributorUseCase, ContributorRepository {
    // This is a bit hacky, but we need to separate contributors/users later
    override fun findById(id: ContributorId): Optional<Contributor> =
        /*userRepository
            .findById(id.value)
            .map(UserEntity::toContributor)*/
        orkgUserRepository.findUserInOldIDOrKeycloakID(id.value).map(ORKGUserEntity::toContributor)

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
        /*userRepository
            .findByOrganizationId(id.value)
            .map(UserEntity::toContributor)*/
        orkgUserRepository
            .findByOrganizationId(id.value)
            .map(ORKGUserEntity::toContributor)

    fun findUsersByObservatoryId(id: ObservatoryId): Iterable<Contributor> =
        /*userRepository
            .findByObservatoryId(id.value)
            .map(UserEntity::toContributor)*/
        orkgUserRepository
            .findByObservatoryId(id.value)
            .map(ORKGUserEntity::toContributor)

    override fun byId(id: ContributorId): Optional<Contributor> = findById(id)
}
