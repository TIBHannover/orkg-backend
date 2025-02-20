package org.orkg.community.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId

interface DummyDataUseCases {
    fun updateOrganizationAndObservatory(
        contributorId: ContributorId,
        organizationId: OrganizationId,
        observatoryId: ObservatoryId,
    )
}
