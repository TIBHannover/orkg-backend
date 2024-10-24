package org.orkg.community.input

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.internal.MD5Hash

interface CreateContributorUseCase {
    fun create(command: CreateCommand): ContributorId

    data class CreateCommand(
        val id: ContributorId,
        val name: String,
        val joinedAt: OffsetDateTime,
        val organizationId: OrganizationId = OrganizationId.UNKNOWN,
        val observatoryId: ObservatoryId = ObservatoryId.UNKNOWN,
        val emailMD5: MD5Hash,
        val isCurator: Boolean = false,
        val isAdmin: Boolean = false,
    )
}

interface DeleteContributorUseCase {
    fun deleteAll()
}
