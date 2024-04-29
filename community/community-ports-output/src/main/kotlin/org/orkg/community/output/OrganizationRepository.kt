package org.orkg.community.output

import java.util.*
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.Organization
import org.orkg.community.domain.OrganizationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrganizationRepository {
    fun save(organization: Organization)

    fun deleteAll()

    fun allMembers(id: OrganizationId, pageable: Pageable): Page<Contributor>

    fun findById(id: OrganizationId): Optional<Organization>

    fun findByDisplayId(name: String): Optional<Organization>

    fun findByName(name: String): Optional<Organization>

    fun findByType(type: OrganizationType): List<Organization>

    fun count(): Long
}
