package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val id = "${'$'}id"

interface Neo4jThingRepository : Neo4jRepository<Neo4jThing, Long> {

    override fun findAll(): Iterable<Neo4jThing>

    @Query("MATCH (node:`Thing`) WHERE node.`resource_id`=$id OR node.`literal_id`=$id OR node.`predicate_id`=$id OR node.`class_id`=$id RETURN node")
    fun findByThingId(id: ThingId): Optional<Neo4jThing>
}
