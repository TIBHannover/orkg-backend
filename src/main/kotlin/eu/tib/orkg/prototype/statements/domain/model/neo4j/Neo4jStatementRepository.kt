package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

/**
 * Partial query that matches a statement.
 * Queries using this partial query must use `rel` as the binding name for predicates, `sub` for subjects, and `obj` for objects.
 */
private const val MATCH_STATEMENT =
    """MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`)"""

/**
 * Partial query that returns the statement, with the id and the created at date.
 * Queries using this partial query must use `rel` as the binding name for predicates, `sub` for subjects, and `obj` for objects.
 */
private const val RETURN_STATEMENT =
    """RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at"""

// Custom queries

private const val WHERE_SUBJECT_ID_IN =
    """WHERE sub.`resource_id` IN {0} OR sub.`literal_id` IN {0} OR sub.`predicate_id` IN {0} OR sub.`class_id` IN {0}"""

private const val WHERE_OBJECT_ID_IN =
    """WHERE obj.`resource_id` IN {0} OR obj.`literal_id` IN {0} OR obj.`predicate_id` IN {0} OR obj.`class_id` IN {0}"""

interface Neo4jStatementRepository :
    Neo4jRepository<Neo4jStatement, Long> {

    override fun findAll(depth: Int): Iterable<Neo4jStatement>

    override fun findById(id: Long): Optional<Neo4jStatement>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatement>

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`) WHERE sub.`resource_id`={0} OR sub.`literal_id`={0} OR sub.`predicate_id`={0} OR sub.`class_id`={0} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllBySubject(subjectId: String, pagination: Pageable): Slice<Neo4jStatement>

    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Slice<Neo4jStatement>

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`) WHERE obj.`resource_id`={0} OR obj.`literal_id`={0} OR obj.`predicate_id`={0} OR obj.`class_id`={0} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByObject(objectId: String, pagination: Pageable): Slice<Neo4jStatement>

    @Query("""MATCH (p:`Thing`)-[*]->() WHERE p.`resource_id`={0} OR p.`literal_id`={0} OR p.`predicate_id`={0} OR p.`class_id`={0} RETURN COUNT(p)""")
    fun countByIdRecursive(paperId: String): Int

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`) WHERE (obj.`resource_id`={0} OR obj.`literal_id`={0} OR obj.`predicate_id`={0} OR obj.`class_id`={0}) AND rel.`predicate_id`={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatement>

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Thing`) WHERE (sub.`resource_id`={0} OR sub.`literal_id`={0} OR sub.`predicate_id`={0} OR sub.`class_id`={0}) AND rel.`predicate_id`={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatement>

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Literal`) WHERE rel.`predicate_id`={0} AND obj.`label`={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Slice<Neo4jStatement>

    @Query("MATCH (sub:`Thing`)-[rel:`RELATED`]->(obj:`Literal`) WHERE {2} IN labels(sub) AND rel.`predicate_id`={0} AND obj.`label`={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Slice<Neo4jStatement>

    @Query("""$MATCH_STATEMENT $WHERE_SUBJECT_ID_IN $RETURN_STATEMENT""")
    fun findAllBySubjects(
        subjectIds: List<String>,
        pagination: Pageable
    ): Page<Neo4jStatement>

    @Query("""$MATCH_STATEMENT $WHERE_OBJECT_ID_IN $RETURN_STATEMENT""")
    fun findAllByObjects(
        subjectIds: List<String>,
        pagination: Pageable
    ): Page<Neo4jStatement>
}
