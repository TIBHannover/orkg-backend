package eu.tib.orkg.prototype.community.services

import eu.tib.orkg.prototype.community.api.DummyDataUseCases
import eu.tib.orkg.prototype.community.api.ObservatoryAuthUseCases
import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.auth.domain.UserNotFound
import eu.tib.orkg.prototype.auth.spi.UserRepository
import java.util.*
import org.springframework.stereotype.Component

// TODO: This is wrong, but we need the refactoring. It should not be left as is.
@Component
class ObservatoryAndOrganizationService(
    private val repository: UserRepository,
) : ObservatoryAuthUseCases, DummyDataUseCases {
    override fun updateOrganizationAndObservatory(
        userId: UUID,
        organizationId: UUID?,
        observatoryId: UUID?
    ) {
        var user = repository.findById(userId).orElseThrow { throw RuntimeException("No user with ID $userId") }
        user = user.copy(organizationId = organizationId, observatoryId = observatoryId)
        repository.save(user)
    }

    override fun addUserObservatory(
        observatoryId: UUID,
        organizationId: UUID,
        contributor: User
    ): User {
        // FIXME: check if user exists?
        val user = contributor.copy(
            observatoryId = observatoryId,
            organizationId = organizationId,
        )
        repository.save(user)
        return user
    }

    override fun deleteUserObservatory(contributor: UUID) {
        var user = repository.findById(contributor).orElseThrow { UserNotFound(contributor) }
        user = user.copy(observatoryId = null, organizationId = null)
        repository.save(user)
    }
}
