package eu.tib.orkg.prototype.contributions.application.ports.input

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.Optional

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
    fun byId(id: ContributorId): Optional<Contributor>

    fun listContributors(): List<Contributor>
}
