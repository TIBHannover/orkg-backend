package org.orkg.graph.adapter.output.neo4j.internal

import java.util.*
import org.orkg.common.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.transaction.annotation.Transactional

private const val id = "${'$'}id"
private const val query = "${'$'}query"
private const val label = "${'$'}label"
private const val minLabelLength = "${'$'}minLabelLength"

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_predicate_on_label"

interface Neo4jPredicateRepository : Neo4jRepository<Neo4jPredicate, ThingId> {
    override fun existsById(id: ThingId): Boolean

    override fun findAll(pageable: Pageable): Page<Neo4jPredicate>

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
    fun findAllByLabel(query: String, label: String, pageable: Pageable): Page<Neo4jPredicate>

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
    fun findAllByLabelContaining(label: String, minLabelLength: Int, pageable: Pageable): Page<Neo4jPredicate>

    override fun findById(id: ThingId): Optional<Neo4jPredicate>

    @Transactional
    override fun deleteById(id: ThingId)

    @Query("""
CALL {
    MATCH (:Predicate {id: $id})<-[r:RELATED]-()
    RETURN r
    UNION ALL
    MATCH (:Predicate {id: $id})<-[r:VALUE]-()
    RETURN r
    UNION ALL
    MATCH ()-[r:RELATED {predicate_id: $id}]-()
    RETURN r
}
WITH r
RETURN COUNT(r) > 0 AS count""")
    fun isInUse(id: ThingId): Boolean
}
