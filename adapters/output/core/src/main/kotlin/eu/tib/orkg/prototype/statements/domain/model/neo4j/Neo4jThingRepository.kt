package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

interface Neo4jThingRepository : Neo4jRepository<Neo4jThing, Long> {

    override fun findAll(): List<Neo4jThing>

    override fun findAll(pageable: Pageable): Page<Neo4jThing>

    @Query("MATCH (node:`Thing`) WHERE node.`resource_id`=$id OR node.`literal_id`=$id OR node.`predicate_id`=$id OR node.`class_id`=$id RETURN node")
    fun findByThingId(id: String?): Optional<Neo4jThing>
}
