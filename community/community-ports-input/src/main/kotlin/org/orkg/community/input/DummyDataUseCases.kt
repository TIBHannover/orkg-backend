package org.orkg.community.input

import java.util.*

interface DummyDataUseCases {
    fun updateOrganizationAndObservatory(userId: UUID, organizationId: UUID?, observatoryId: UUID?)
}
