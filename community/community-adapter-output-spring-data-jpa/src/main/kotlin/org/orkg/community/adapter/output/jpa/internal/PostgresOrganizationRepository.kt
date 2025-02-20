package org.orkg.community.adapter.output.jpa.internal

import org.orkg.community.domain.OrganizationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface PostgresOrganizationRepository : JpaRepository<OrganizationEntity, UUID> {
    @Query("""FROM OrganizationEntity WHERE (displayId=:name or displayId=LOWER(:name))""")
    fun findByDisplayId(name: String): Optional<OrganizationEntity>

    fun findByName(name: String): Optional<OrganizationEntity>

    fun findByType(type: OrganizationType): List<OrganizationEntity>
}
