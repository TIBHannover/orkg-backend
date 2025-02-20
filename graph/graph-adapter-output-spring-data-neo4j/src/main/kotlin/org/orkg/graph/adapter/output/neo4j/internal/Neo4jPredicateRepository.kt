package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val ID = "${'$'}id"

interface Neo4jPredicateRepository : Neo4jRepository<Neo4jPredicate, ThingId> {
    override fun deleteById(id: ThingId)

    @Query(
        """
CALL () {
    MATCH (:Predicate {id: $ID})<-[r:RELATED]-()
    RETURN r
    UNION ALL
    MATCH (:Predicate {id: $ID})<-[r:VALUE]-()
    RETURN r
    UNION ALL
    MATCH ()-[r:RELATED {predicate_id: $ID}]-()
    RETURN r
}
WITH r
RETURN COUNT(r) > 0 AS count"""
    )
    fun isInUse(id: ThingId): Boolean
}
