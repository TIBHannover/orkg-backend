package eu.tib.orkg.prototype.contributions.spi

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributorRepository {
    fun findById(id: ContributorId): Optional<Contributor>

    fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Contributor>

    fun findAllByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Contributor>

    fun findAllByIds(ids: List<ContributorId>): List<Contributor>
}
