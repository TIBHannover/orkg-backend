package org.orkg.graph.adapter.output.neo4j.internal

import java.util.*
import org.orkg.common.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.transaction.annotation.Transactional

private const val ids = "${'$'}ids"
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

    // @Query was added manually because of a strange bug that it is not reproducible. It seems that the OGM generates
    // a query containing a string literal when the set only has one element, which the driver refused as an invalid
    // query (which it is). It only happens under certain circumstances which are not reproducible in a test. I checked
    // the driver version and everything else that came to mind. No idea what is wrong. This seems to work. -- MP
    @Query("""MATCH (n:`Predicate`) WHERE n.id in $ids RETURN n""")
    fun findAllByIdIn(ids: Set<ThingId>): Iterable<Neo4jPredicate>

    @Transactional
    override fun deleteById(id: ThingId)
}
