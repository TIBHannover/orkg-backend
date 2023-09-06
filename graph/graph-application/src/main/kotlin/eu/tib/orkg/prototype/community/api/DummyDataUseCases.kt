package eu.tib.orkg.prototype.community.api

import java.util.*

interface DummyDataUseCases {
    fun updateOrganizationAndObservatory(userId: UUID, organizationId: UUID?, observatoryId: UUID?)
}
