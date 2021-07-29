package eu.tib.orkg.prototype.statements.domain.model.jpa

import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostgresOrganizationRepository : JpaRepository<OrganizationEntity, UUID> {

    @Query("""FROM OrganizationEntity WHERE (displayId=:name or displayId=LOWER(:name))""")
    fun findByDisplayId(name: String): Optional<OrganizationEntity>

    fun findByName(name: String): Optional<OrganizationEntity>
}
