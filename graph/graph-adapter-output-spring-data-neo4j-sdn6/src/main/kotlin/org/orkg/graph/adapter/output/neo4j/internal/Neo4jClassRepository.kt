package org.orkg.graph.adapter.output.neo4j.internal

import java.util.*
import org.orkg.common.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val ids = "${'$'}ids"
private const val query = "${'$'}query"
private const val label = "${'$'}label"
private const val minLabelLength = "${'$'}minLabelLength"

private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_class_on_label"

interface Neo4jClassRepository : Neo4jRepository<Neo4jClass, ThingId> {
    override fun existsById(id: ThingId): Boolean

    // Set operations are a bit tricky in Cypher. It only knows lists, and order matters there. APOC to the rescue!
    @Query("""MATCH (c:`Class`) WHERE c.id IN $ids RETURN apoc.coll.containsAll(collect(c.id), $ids) AS result""")
    fun existsAllById(ids: Iterable<ThingId>): Boolean

    override fun findById(id: ThingId): Optional<Neo4jClass>

    fun findAllByIdIn(ids: Iterable<ThingId>, pageable: Pageable): Page<Neo4jClass>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $query)
YIELD node
WHERE toLower(node.label) = toLower($label)
WITH node
ORDER BY node.created_at ASC
RETURN node, [[(node)-[r:`RELATED`]->(t:`Thing`) | [r, t]]] $PAGE_PARAMS""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $query)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN COUNT(node)""")
    fun findAllByLabel(query: String, label: String, pageable: Pageable): Page<Neo4jClass>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node, score
WHERE SIZE(node.label) >= $minLabelLength
WITH node, score
ORDER BY SIZE(node.label) ASC, score DESC, node.created_at ASC
RETURN node, [[(node)-[r:`RELATED`]->(t:`Thing`) | [r, t]]] $PAGE_PARAMS""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE SIZE(node.label) >= $minLabelLength
RETURN COUNT(node)""")
    fun findAllByLabelContaining(label: String, minLabelLength: Int, pageable: Pageable): Page<Neo4jClass>

    fun findByUri(uri: String): Optional<Neo4jClass>

    @Query("""MATCH (c:Class) DETACH DELETE c""")
    override fun deleteAll()
}
