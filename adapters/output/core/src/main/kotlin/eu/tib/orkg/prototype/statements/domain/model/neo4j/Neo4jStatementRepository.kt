package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.id
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.ids
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.label
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.objectId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.predicateId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.subjectClass
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.subjectId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

/**
 * Partial query that matches a statement.
 * Queries using this partial query must use `rel` as the binding name for predicates, `sub` for subjects, and `obj` for objects.
 */
internal const val MATCH_STATEMENT =
    """MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`)"""

internal const val MATCH_STATEMENT_WITH_LITERAL =
    """MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Literal`)"""

/**
 * Partial query that returns the statement, with the id and the created at date.
 * Queries using this partial query must use `rel` as the binding name for predicates, `sub` for subjects, and `obj` for objects.
 */
internal const val RETURN_STATEMENT =
    """RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at"""

/**
 * Partial query that returns the number of statements (relationships) in [Query.countQuery] queries.
 */
internal const val RETURN_COUNT = "RETURN count(rel) AS count"

/**
 * Partial query that "flattens" the object into "columns" that can be sorted by SDN.
 */
internal const val WITH_SORTABLE_FIELDS =
    """WITH sub, obj, rel, rel.created_at AS created_at, rel.created_by AS created_by"""

// Custom queries

internal const val BY_SUBJECT_ID =
    """WHERE sub.`resource_id`=$id OR sub.`literal_id`=$id OR sub.`predicate_id`=$id OR sub.`class_id`=$id"""

internal const val BY_OBJECT_ID =
    """WHERE obj.`resource_id`=$id OR obj.`literal_id`=$id OR obj.`predicate_id`=$id OR obj.`class_id`=$id"""

internal const val WHERE_SUBJECT_ID_IN =
    """WHERE sub.`resource_id` IN $ids OR sub.`literal_id` IN $ids OR sub.`predicate_id` IN $ids OR sub.`class_id` IN $ids"""

internal const val WHERE_OBJECT_ID_IN =
    """WHERE obj.`resource_id` IN $ids OR obj.`literal_id` IN $ids OR obj.`predicate_id` IN $ids OR obj.`class_id` IN $ids"""

interface Neo4jStatementRepository :
    Neo4jRepository<Neo4jStatement, Long> {

    override fun findById(id: Long): Optional<Neo4jStatement>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatement>

    @Query("$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllBySubject(id: String, pagination: Pageable): Page<Neo4jStatement>

    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Page<Neo4jStatement>

    @Query("$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllByObject(id: String, pagination: Pageable): Page<Neo4jStatement>

    @Query("""MATCH (p:`Thing`)-[*]->() WHERE p.`resource_id`=$id OR p.`literal_id`=$id OR p.`predicate_id`=$id OR p.`class_id`=$id RETURN COUNT(p)""")
    fun countByIdRecursive(id: String): Int

    @Query("$MATCH_STATEMENT WHERE (obj.`resource_id`=$objectId OR obj.`literal_id`=$objectId OR obj.`predicate_id`=$objectId OR obj.`class_id`=$objectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT WHERE (obj.`resource_id`=$objectId OR obj.`literal_id`=$objectId OR obj.`predicate_id`=$objectId OR obj.`class_id`=$objectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query("$MATCH_STATEMENT WHERE (sub.`resource_id`= $subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
    countQuery = "$MATCH_STATEMENT WHERE (sub.`resource_id`= $subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId) AND rel.`predicate_id`=$predicateId $WITH_SORTABLE_FIELDS $RETURN_COUNT")
    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        "$MATCH_STATEMENT_WITH_LITERAL WHERE rel.`predicate_id`= $predicateId AND obj.`label`= $label $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
        countQuery = "$MATCH_STATEMENT_WITH_LITERAL WHERE rel.`predicate_id`= $predicateId AND obj.`label`= $label $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        "$MATCH_STATEMENT_WITH_LITERAL WHERE $subjectClass IN labels(sub) AND rel.`predicate_id`= $predicateId AND obj.`label`= $label $WITH_SORTABLE_FIELDS $RETURN_STATEMENT",
        countQuery = "$MATCH_STATEMENT_WITH_LITERAL WHERE $subjectClass IN labels(sub) AND rel.`predicate_id`= $predicateId AND obj.`label`= $label $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        """$MATCH_STATEMENT $WHERE_SUBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_STATEMENT""",
        countQuery = "$MATCH_STATEMENT $WHERE_OBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllBySubjects(
        ids: List<String>,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        """$MATCH_STATEMENT $WHERE_OBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_STATEMENT""",
        countQuery = "$MATCH_STATEMENT $WHERE_OBJECT_ID_IN $WITH_SORTABLE_FIELDS $RETURN_COUNT"
    )
    fun findAllByObjects(
        ids: List<String>,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query(
        """MATCH (n:Thing)
WHERE n.resource_id = $id OR n.literal_id = $id OR n.class_id = $id OR n.predicate_id = $id
CALL apoc.path.subgraphAll(n, ${'$'}configuration)
YIELD relationships
UNWIND relationships as rel
RETURN startNode(rel) as subject, rel as predicate, endNode(rel) as object
ORDER BY rel.created_at DESC"""
    )
    fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<Neo4jStatement>
}
