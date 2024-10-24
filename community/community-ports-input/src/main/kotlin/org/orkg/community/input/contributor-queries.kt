package org.orkg.community.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.community.domain.Contributor

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

    fun findAllByIds(ids: List<ContributorId>): List<Contributor>
}
