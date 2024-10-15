package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.input.DummyDataUseCases
import org.orkg.community.input.ObservatoryAuthUseCases
import org.orkg.community.output.ContributorRepository
import org.springframework.stereotype.Component

// TODO: This is wrong, but we need the refactoring. It should not be left as is.
@Component
class ObservatoryAndOrganizationService(
    private val repository: ContributorRepository,
) : ObservatoryAuthUseCases, DummyDataUseCases {
    override fun updateOrganizationAndObservatory(
        contributorId: ContributorId,
        organizationId: OrganizationId,
        observatoryId: ObservatoryId
    ) {
        var user = repository.findById(contributorId)
            .orElseThrow { ContributorNotFound(contributorId) }
        user = user.copy(organizationId = organizationId, observatoryId = observatoryId)
        repository.save(user)
    }

    override fun addUserObservatory(
        observatoryId: ObservatoryId,
        organizationId: OrganizationId,
        contributorId: ContributorId
    ): Contributor {
        val contributor = repository.findById(contributorId)
            .orElseThrow { ContributorNotFound(contributorId) }
        val user = contributor.copy(
            observatoryId = observatoryId,
            organizationId = organizationId,
        )
        repository.save(user)
        return user
    }

    override fun deleteUserObservatory(contributorId: ContributorId) {
        var user = repository.findById(contributorId)
            .orElseThrow { ContributorNotFound(contributorId) }
        user = user.copy(
            observatoryId = ObservatoryId.UNKNOWN,
            organizationId = OrganizationId.UNKNOWN
        )
        repository.save(user)
    }
}
