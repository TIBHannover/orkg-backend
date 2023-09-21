package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.transaction.annotation.Transactional

private const val ids = "${'$'}ids"
private const val query = "${'$'}query"
private const val label = "${'$'}label"
private const val minLabelLength = "${'$'}minLabelLength"

private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_predicate_on_label"

interface Neo4jPredicateRepository : Neo4jRepository<Neo4jPredicate, Long> {
    fun existsById(id: ThingId): Boolean

    @Deprecated("Migrate to the pageable version.")
    override fun findAll(): Iterable<Neo4jPredicate>

    override fun findAll(pageable: Pageable): Page<Neo4jPredicate>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $query)
YIELD node
WHERE toLower(node.label) = toLower($label)
WITH node
ORDER BY node.created_at ASC
RETURN node, [[(node)-[r:`RELATED`]->(t:`Thing`) | [r, t]]]""",
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
RETURN node, [[(node)-[r:`RELATED`]->(t:`Thing`) | [r, t]]]""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE SIZE(node.label) >= $minLabelLength
RETURN COUNT(node)""")
    fun findAllByLabelContaining(label: String, minLabelLength: Int, pageable: Pageable): Page<Neo4jPredicate>

    fun findById(id: ThingId?): Optional<Neo4jPredicate>

    // @Query was added manually because of a strange bug that it is not reproducible. It seems that the OGM generates
    // a query containing a string literal when the set only has one element, which the driver refused as an invalid
    // query (which it is). It only happens under certain circumstances which are not reproducible in a test. I checked
    // the driver version and everything else that came to mind. No idea what is wrong. This seems to work. -- MP
    @Query("""MATCH (n:`Predicate`) WHERE n.id in $ids RETURN n""")
    fun findAllByIdIn(ids: Set<ThingId>): Iterable<Neo4jPredicate>

    // The return type has to be Iterable<Long> due to type erasure as java.lang.Long or Iterable<java.lang.Long> is
    // required by Spring, but we want to use kotlin.Long whenever possible
    @Transactional
    fun deleteById(id: ThingId): Iterable<Long>
}