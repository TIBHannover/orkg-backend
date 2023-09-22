package eu.tib.orkg.prototype.community.spi

import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.Contributor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrganizationRepository {
    fun allMembers(id: OrganizationId, pageable: Pageable): Page<Contributor>
}
