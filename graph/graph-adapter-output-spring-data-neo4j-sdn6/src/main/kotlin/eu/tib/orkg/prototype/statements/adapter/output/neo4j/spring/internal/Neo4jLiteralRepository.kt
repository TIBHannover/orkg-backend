package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val query = "${'$'}query"
private const val label = "${'$'}label"
private const val minLabelLength = "${'$'}minLabelLength"

private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_literal_on_label"

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, ThingId> {
    override fun existsById(id: ThingId): Boolean

    override fun findById(id: ThingId): Optional<Neo4jLiteral>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $query)
YIELD node
WHERE toLower(node.label) = toLower($label)
WITH node
ORDER BY node.created_at ASC
RETURN node $PAGE_PARAMS""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $query)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN COUNT(node)""")
    fun findAllByLabel(query: String, label: String, pageable: Pageable): Page<Neo4jLiteral>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node, score
WHERE SIZE(node.label) >= $minLabelLength
WITH node, score
ORDER BY SIZE(node.label) ASC, score DESC, node.created_at ASC
RETURN node $PAGE_PARAMS""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE SIZE(node.label) >= $minLabelLength
RETURN COUNT(node)""")
    fun findAllByLabelContaining(label: String, minLabelLength: Int, pageable: Pageable): Page<Neo4jLiteral>
}
