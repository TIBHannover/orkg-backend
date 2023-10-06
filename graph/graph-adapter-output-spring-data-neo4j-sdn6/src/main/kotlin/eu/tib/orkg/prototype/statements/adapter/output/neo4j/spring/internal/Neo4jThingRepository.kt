package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val ids = "${'$'}ids"

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jThingRepository : Neo4jRepository<Neo4jThing, ThingId> {

    @Query("""MATCH (n:Thing) OPTIONAL MATCH (n)-[r:RELATED]->(m:Thing) RETURN n, COLLECT(r) AS relations, COLLECT(m) AS relatedNodes $PAGE_PARAMS""",
        countQuery = """MATCH (n:Thing) RETURN COUNT(n)""")
    override fun findAll(pageable: Pageable): Page<Neo4jThing>

    @Query("""
UNWIND $ids AS id
MATCH (node:Thing {id: id})
RETURN apoc.coll.containsAll(collect(node.id), $ids) AS result""")
    fun existsAll(ids: Set<ThingId>): Boolean
}
