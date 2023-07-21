package eu.tib.orkg.prototype.community.adapter.output.jpa.internal

import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostgresOrganizationRepository : JpaRepository<OrganizationEntity, UUID> {

    @Query("""FROM OrganizationEntity WHERE (displayId=:name or displayId=LOWER(:name))""")
    fun findByDisplayId(name: String): Optional<OrganizationEntity>

    fun findByName(name: String): Optional<OrganizationEntity>

    fun findByType(type: OrganizationType): List<OrganizationEntity>
}
