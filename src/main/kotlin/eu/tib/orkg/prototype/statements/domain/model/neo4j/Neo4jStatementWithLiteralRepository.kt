package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.Optional

interface Neo4jStatementWithLiteralRepository :
    Neo4jRepository<Neo4jStatementWithLiteral, Long> {

    override fun findAll(depth: Int): Iterable<Neo4jStatementWithLiteral>

    override fun findById(id: Long): Optional<Neo4jStatementWithLiteral>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatementWithLiteral>

    @Query("MATCH (sub:`Resource`)-[rel:`HAS_VALUE_OF`]->(obj:`Literal`) WHERE sub.`resource_id`={0} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllBySubject(subjectId: ResourceId, pagination: Pageable): Slice<Neo4jStatementWithLiteral>

    @Query("MATCH (sub:`Resource`)-[rel:`HAS_VALUE_OF`]->(obj:`Literal`) WHERE sub.`resource_id`={0} AND rel.`predicate_id`={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatementWithLiteral>

    @Query("MATCH (sub:`Resource`)-[rel:`HAS_VALUE_OF`]->(obj:`Literal`) WHERE obj.`literal_id`={0} AND rel.`predicate_id`={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByObjectAndPredicate(
        resourceId: LiteralId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatementWithLiteral>

    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Slice<Neo4jStatementWithLiteral>

    @Query("MATCH (sub:`Resource`)-[rel:`HAS_VALUE_OF`]->(obj:`Literal`) WHERE obj.`literal_id`={0} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByObject(objectId: LiteralId, pagination: Pageable): Slice<Neo4jStatementWithLiteral>
}
