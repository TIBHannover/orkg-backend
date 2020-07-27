package eu.tib.orkg.prototype.contributions.domain.model

import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
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

    fun findOrganizationById(userId: UUID): Optional<Contributor> =
        userRepository
            .findOrganizationById(userId)
            .map(UserEntity::toContributor)

    fun findUsersByOrganizationId(id: UUID): Iterable<Contributor> =
        userRepository
            .findUsersByOrganizationId(id)
            .map(UserEntity::toContributor)

    fun findUsersByObservatoryId(id: UUID): Iterable<Contributor> =
        userRepository
            .findUsersByObservatoryId(id)
            .map(UserEntity::toContributor)
}
