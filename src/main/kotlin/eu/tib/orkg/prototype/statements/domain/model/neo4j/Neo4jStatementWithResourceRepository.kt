package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.Optional

interface Neo4jStatementWithResourceRepository :
    Neo4jRepository<Neo4jStatementWithResource, Long> {

    override fun findAll(): Iterable<Neo4jStatementWithResource>

    override fun findById(id: Long): Optional<Neo4jStatementWithResource>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatementWithResource>

    // TODO: Return type as Slice not Page because Slice don't need a count query which might be more efficient
    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE sub.`resource_id`={0} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllBySubject(subjectId: ResourceId, pagination: Pageable): Slice<Neo4jStatementWithResource>

    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE sub.`resource_id`={0} AND rel.predicate_id={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatementWithResource>

    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE obj.`resource_id`={0} AND rel.predicate_id={1} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByObjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatementWithResource>

    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Slice<Neo4jStatementWithResource>

    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE obj.`resource_id`={0} RETURN rel, sub, obj, rel.statement_id AS id, rel.created_at AS created_at")
    fun findAllByObject(resourceId: ResourceId, pagination: Pageable): Slice<Neo4jStatementWithResource>
}
