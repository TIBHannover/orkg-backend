package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val ids = "${'$'}ids"
private const val label = "${'$'}label"

private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_class_on_label"

interface Neo4jClassRepository : Neo4jRepository<Neo4jClass, Long> {
    fun existsByClassId(id: ClassId): Boolean

    // Set operations are a bit tricky in Cypher. It only knows lists, and order matters there. APOC to the rescue!
    @Query("""MATCH (c:`Class`) WHERE c.class_id IN $ids RETURN apoc.coll.containsAll(collect(c.class_id), $ids) AS result""")
    fun existsAllByClassId(ids: Iterable<ClassId>): Boolean

    fun findByClassId(id: ClassId?): Optional<Neo4jClass>

    fun findAllByClassIdIn(ids: Iterable<ClassId>, pageable: Pageable): Page<Neo4jClass>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN node $PAGE_PARAMS""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN COUNT(node)""")
    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jClass>

    @Query("""
WITH SIZE($label) AS size
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
RETURN node $PAGE_PARAMS""",
        countQuery = """
WITH SIZE($label) AS size
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
RETURN COUNT(node)""")
    fun findAllByLabelContaining(label: String, pageable: Pageable): Page<Neo4jClass>

    fun findByUri(uri: String): Optional<Neo4jClass>
}
