package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.input.DummyDataUseCases
import org.orkg.community.input.ObservatoryAuthUseCases
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.springframework.stereotype.Component

// TODO: This is wrong, but we need the refactoring. It should not be left as is.
@Component
class ObservatoryAndOrganizationService(
    private val repository: ContributorRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
) : ObservatoryAuthUseCases,
    DummyDataUseCases {
    override fun updateOrganizationAndObservatory(
        contributorId: ContributorId,
        organizationId: OrganizationId,
        observatoryId: ObservatoryId,
    ) {
        var user = repository.findById(contributorId)
            .orElseThrow { ContributorNotFound(contributorId) }
        user = user.copy(organizationId = organizationId, observatoryId = observatoryId)
        repository.save(user)
    }

    override fun addUserObservatory(
        observatoryId: ObservatoryId,
        organizationId: OrganizationId,
        contributorId: ContributorId,
    ): Contributor {
        val contributor = repository.findById(contributorId)
            .orElseThrow { ContributorNotFound(contributorId) }
        if (observatoryId != ObservatoryId.UNKNOWN && !observatoryRepository.existsById(observatoryId)) {
            throw ObservatoryNotFound(observatoryId)
        }
        if (organizationId != OrganizationId.UNKNOWN && organizationRepository.findById(organizationId).isEmpty) {
            throw OrganizationNotFound(organizationId)
        }
        val updated = contributor.copy(
            observatoryId = observatoryId,
            organizationId = organizationId,
        )
        if (updated != contributor) {
            repository.save(updated)
        }
        return updated
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
