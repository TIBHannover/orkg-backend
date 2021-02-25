package eu.tib.orkg.prototype.statements.domain.model.neo4j

import java.util.Optional
import java.util.UUID
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jOrganizationRepository : Neo4jRepository<Neo4jOrganization, UUID> {

    @Query("""MATCH (n:Organization {organization_id: {0}}) RETURN n""")
    fun findByOrganizationId(id: UUID): Optional<Neo4jOrganization>
}
