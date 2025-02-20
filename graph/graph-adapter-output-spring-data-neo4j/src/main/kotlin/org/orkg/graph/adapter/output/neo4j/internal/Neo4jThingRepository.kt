package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val ID = "${'$'}id"
private const val IDS = "${'$'}ids"

private const val ORDER_BY_PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jThingRepository : Neo4jRepository<Neo4jThing, ThingId> {
    @Query(
        """MATCH (n:Thing) RETURN n $ORDER_BY_PAGE_PARAMS""",
        countQuery = """MATCH (n:Thing) RETURN COUNT(n)"""
    )
    override fun findAll(pageable: Pageable): Page<Neo4jThing>

    @Query(
        """
UNWIND $IDS AS id
MATCH (node:Thing {id: id})
RETURN apoc.coll.containsAll(collect(node.id), $IDS) AS result"""
    )
    fun existsAll(ids: Set<ThingId>): Boolean

    @Query(
        """
CALL () {
    MATCH (:Thing {id: $ID})<-[r:RELATED]-()
    RETURN r
    UNION ALL
    MATCH (:Thing {id: $ID})<-[r:VALUE]-()
    RETURN r
}
WITH r
RETURN COUNT(r) > 0 AS count"""
    )
    fun isUsedAsObject(id: ThingId): Boolean
}
