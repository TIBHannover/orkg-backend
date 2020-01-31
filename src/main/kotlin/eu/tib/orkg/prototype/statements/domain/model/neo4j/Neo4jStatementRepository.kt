package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.Optional

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

    @Query("""MATCH (p:`Thing`})-[*]->() WHERE p.`resource_id`={0} OR p.`literal_id`={0} OR p.`predicate_id`={0} OR p.`class_id`={0} RETURN COUNT(p)""")
    fun countByIdRecursive(paperId: String): Int

    @Query(
        "MATCH (s:Thing)-[p:RELATED]->(o:Thing)\n" +
            "WITH CASE \n" +
            " WHEN 'Resource' IN LABELS(s) THEN s.resource_id\n" +
            " WHEN 'Literal' IN LABELS(s) THEN s.literal_id\n" +
            " WHEN 'Predicate' IN LABELS(s) THEN s.predicate_id\n" +
            " ELSE s.class_id\n" +
            "END AS subjectId,\n" +
            "CASE \n" +
            " WHEN 'Resource' IN LABELS(o) THEN o.resource_id\n" +
            " WHEN 'Literal' IN LABELS(o) THEN o.literal_id\n" +
            " WHEN 'Predicate' IN LABELS(o) THEN o.predicate_id\n" +
            " ELSE o.class_id\n" +
            "END AS objectId, p.predicate_id AS predicateId\n" +
            "RETURN subjectId, predicateId, objectId"
    )
    fun listByIds(): List<Triple<String, PredicateId, String>>
}
