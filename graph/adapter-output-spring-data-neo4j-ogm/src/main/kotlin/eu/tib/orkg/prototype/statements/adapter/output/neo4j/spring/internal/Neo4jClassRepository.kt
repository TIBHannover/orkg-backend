package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val ids = "${'$'}ids"
private const val label = "${'$'}label"

private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_class_on_label"

interface Neo4jClassRepository : Neo4jRepository<Neo4jClass, Long> {
    fun existsById(id: ThingId): Boolean

    // Set operations are a bit tricky in Cypher. It only knows lists, and order matters there. APOC to the rescue!
    @Query("""MATCH (c:`Class`) WHERE c.id IN $ids RETURN apoc.coll.containsAll(collect(c.id), $ids) AS result""")
    fun existsAllById(ids: Iterable<ThingId>): Boolean

    fun findById(id: ThingId?): Optional<Neo4jClass>

    fun findAllByIdIn(ids: Iterable<ThingId>, pageable: Pageable): Page<Neo4jClass>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN node, [[(node)-[r:`RELATED`]->(t:`Thing`) | [r, t]]]""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
WHERE toLower(node.label) = toLower($label)
RETURN COUNT(node)""")
    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jClass>

    @Query("""
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
RETURN node, [[(node)-[r:`RELATED`]->(t:`Thing`) | [r, t]]]""",
        countQuery = """
CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", $label)
YIELD node
RETURN COUNT(node)""")
    fun findAllByLabelContaining(label: String, pageable: Pageable): Page<Neo4jClass>

    fun findByUri(uri: String): Optional<Neo4jClass>

    @Query("""MATCH (c:Class) DETACH DELETE c""")
    override fun deleteAll()
}
