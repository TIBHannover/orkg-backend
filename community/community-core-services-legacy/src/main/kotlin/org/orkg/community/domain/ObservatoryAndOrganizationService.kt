package org.orkg.community.domain

import java.util.*
import org.orkg.auth.domain.User
import org.orkg.auth.domain.UserNotFound
import org.orkg.auth.output.UserRepository
import org.orkg.community.input.DummyDataUseCases
import org.orkg.community.input.ObservatoryAuthUseCases
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
