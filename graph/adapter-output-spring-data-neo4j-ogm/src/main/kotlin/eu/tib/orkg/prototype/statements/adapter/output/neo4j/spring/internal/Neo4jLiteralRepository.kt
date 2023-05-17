package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val label = "${'$'}label"

private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_literal_on_label"

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, Long> {
    fun existsById(id: ThingId): Boolean

    fun findById(id: ThingId?): Optional<Neo4jLiteral>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN COUNT(node)""")
    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jLiteral>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
RETURN node""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
RETURN COUNT(node)""")
    fun findAllByLabelContaining(label: String, pageable: Pageable): Page<Neo4jLiteral>
}
