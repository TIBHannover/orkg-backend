package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

interface Neo4jThingRepository : Neo4jRepository<Neo4jThing, Long> {

    override fun findAll(): List<Neo4jThing>

    @Query("MATCH (node:`Thing`) WHERE node.`resource_id`={0} OR node.`literal_id`={0} OR node.`predicate_id`={0} OR node.`class_id`={0} RETURN node")
    fun findByThingId(id: String?): Optional<Neo4jThing>
}
