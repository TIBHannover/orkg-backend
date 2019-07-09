package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.Optional

interface Neo4jStatementWithResourceRepository :
    Neo4jRepository<Neo4jStatementWithResource, Long> {

    override fun findAll(): Iterable<Neo4jStatementWithResource>

    override fun findById(id: Long): Optional<Neo4jStatementWithResource>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatementWithResource>

    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE sub.`resource_id`={0} RETURN rel, sub, obj")
    fun findAllBySubject(subjectId: ResourceId): Iterable<Neo4jStatementWithResource>

    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE sub.`resource_id`={0} AND rel.predicate_id={1} RETURN rel, sub, obj")
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId
    ): Iterable<Neo4jStatementWithResource>

    fun findAllByPredicateId(predicateId: PredicateId): Iterable<Neo4jStatementWithResource>

    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE obj.`resource_id`={0} RETURN rel, sub, obj")
    fun findAllByObject(resourceId: ResourceId): Iterable<Neo4jStatementWithResource>
}
