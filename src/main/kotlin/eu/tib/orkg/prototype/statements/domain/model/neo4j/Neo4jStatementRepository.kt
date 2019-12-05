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
}
