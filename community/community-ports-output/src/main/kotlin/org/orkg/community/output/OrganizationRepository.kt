package org.orkg.community.output

import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface OrganizationRepository {
    fun save(organization: Organization)

    fun deleteAll()

    fun findAllMembersByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Contributor>

    fun findById(id: OrganizationId): Optional<Organization>

    fun findByDisplayId(name: String): Optional<Organization>

    fun findByName(name: String): Optional<Organization>

    fun findAllByType(type: OrganizationType): List<Organization>

    fun count(): Long
}
