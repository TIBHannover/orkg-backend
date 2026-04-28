package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

private const val IDS = $$"$ids"

interface Neo4jClassRepository : Neo4jRepository<Neo4jClass, ThingId> {
    // Set operations are a bit tricky in Cypher. It only knows lists, and order matters there.
    @Query(
        """
        UNWIND $IDS AS id
        MATCH (c:Class {id: id})
        WITH collect(c.id) AS ids
        RETURN SIZE($IDS) > 0 AND all(id IN $IDS WHERE id IN ids) AS result
        """,
    )
    fun existsAllById(ids: Iterable<ThingId>): Boolean

    fun findByUri(uri: String): Optional<Neo4jClass>

    @Query("""MATCH (c:Class) DETACH DELETE c""")
    override fun deleteAll()
}
