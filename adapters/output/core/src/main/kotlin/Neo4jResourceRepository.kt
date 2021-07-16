package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    fun findByResourceId(id: ResourceId?): Optional<Neo4jResource>
}
