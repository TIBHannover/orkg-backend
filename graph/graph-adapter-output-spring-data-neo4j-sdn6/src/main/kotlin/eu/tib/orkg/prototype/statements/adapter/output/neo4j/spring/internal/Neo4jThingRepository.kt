package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val ids = "${'$'}ids"

interface Neo4jThingRepository : Neo4jRepository<Neo4jThing, ThingId> {
    override fun findById(id: ThingId): Optional<Neo4jThing>

    @Query("""
UNWIND $ids AS id
MATCH (node:Thing {id: id})
RETURN apoc.coll.containsAll(collect(node.id), $ids) AS result""")
    fun existsAll(ids: Set<ThingId>): Boolean
}
