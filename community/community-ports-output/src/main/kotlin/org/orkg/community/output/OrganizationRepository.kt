package org.orkg.community.output

import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrganizationRepository {
    fun allMembers(id: OrganizationId, pageable: Pageable): Page<Contributor>
}
