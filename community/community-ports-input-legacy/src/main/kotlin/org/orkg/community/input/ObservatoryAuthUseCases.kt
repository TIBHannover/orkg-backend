package org.orkg.community.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor

interface ObservatoryAuthUseCases {
    // TODO: More obscure use cases, that we should change or decouple:
    fun addUserObservatory(
        observatoryId: ObservatoryId,
        organizationId: OrganizationId,
        contributorId: ContributorId,
    ): Contributor

    fun deleteUserObservatory(contributorId: ContributorId)
}
