package eu.tib.orkg.prototype.statements.domain.model.neo4j

import java.util.Optional
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jThingRepository : Neo4jRepository<Neo4jThing, Long> {

    override fun findAll(): Iterable<Neo4jThing>

    @Query("MATCH (node:`Thing`) WHERE node.`resource_id`={0} OR node.`literal_id`={0} OR node.`predicate_id`={0} OR node.`class_id`={0} RETURN node")
    fun findByThingId(id: String?): Optional<Neo4jThing>
}
