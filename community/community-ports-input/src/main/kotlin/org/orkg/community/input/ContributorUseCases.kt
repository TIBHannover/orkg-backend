package org.orkg.community.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.internal.MD5Hash
import java.time.OffsetDateTime
import java.util.Optional

interface ContributorUseCases :
    RetrieveContributorUseCase,
    CreateContributorUseCase,
    DeleteContributorUseCase

/**
 * A client retrieves contributor information from the system.
 */
interface RetrieveContributorUseCase {
    /**
     *  Retrieve the information about a specific contributor.
     *
     * @param id A [ContributorId] identifying the contributor.
     * @return A [Contributor] wrapped in an [Optional], or an empty [Optional] otherwise.
     */
    fun findById(id: ContributorId): Optional<Contributor>

    fun findAllById(ids: List<ContributorId>): List<Contributor>
}

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
