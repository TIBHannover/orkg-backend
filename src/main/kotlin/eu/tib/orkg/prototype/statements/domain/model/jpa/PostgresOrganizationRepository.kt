package eu.tib.orkg.prototype.statements.domain.model.jpa

import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface PostgresOrganizationRepository : JpaRepository<OrganizationEntity, UUID> {

    fun findByUriName(name: String): Optional<OrganizationEntity>
}
