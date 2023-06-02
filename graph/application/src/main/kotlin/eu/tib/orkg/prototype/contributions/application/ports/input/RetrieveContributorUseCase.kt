package eu.tib.orkg.prototype.contributions.application.ports.input

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

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

    fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Contributor>

    fun findAllByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Contributor>

    fun findAllByIds(ids: List<ContributorId>): List<Contributor>
}
