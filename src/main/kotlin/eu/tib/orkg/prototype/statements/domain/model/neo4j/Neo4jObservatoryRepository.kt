package eu.tib.orkg.prototype.statements.domain.model.neo4j
import java.util.Optional
import java.util.UUID
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jObservatoryRepository : Neo4jRepository<Neo4jObservatory, UUID> {

    @Query("""MATCH (n:Observatory {name: {0}}) RETURN n""")
    fun findByName(name: String): Optional<Neo4jObservatory>

    @Query("""MATCH (o:Organization {organization_id: {0}}) MATCH (n:Observatory {observatory_id: {1}}) CREATE (o)-[r:BELONGS_TO]->(n) RETURN r""")
    fun createRelationInObservatoryOrganization(organization: UUID, observatory: UUID)
}
