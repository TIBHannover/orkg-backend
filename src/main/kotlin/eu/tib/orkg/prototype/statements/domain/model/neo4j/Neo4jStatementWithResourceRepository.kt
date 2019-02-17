package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.data.neo4j.annotation.*
import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jStatementWithResourceRepository :
    Neo4jRepository<Neo4jStatementWithResource, Long>, Neo4jStatementWithResourceRepositoryCustom {

    override fun findAll(): Iterable<Neo4jStatementWithResource>

    override fun findById(id: Long): Optional<Neo4jStatementWithResource>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatementWithResource>

    @Query("MATCH (s:Resource)-[rel:RELATES_TO]->(:Resource) WHERE s.resource_id={0} RETURN rel")
    fun findAllBySubject(subjectId: ResourceId): Iterable<Neo4jStatementWithResource>

    @Query("MATCH (s:Resource)-[rel:RELATES_TO]->(:Resource) WHERE s.resource_id={0} AND rel.predicate_id={1} RETURN rel")
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId
    ): Iterable<Neo4jStatementWithResource>

    fun findAllByPredicateId(predicateId: PredicateId): Iterable<Neo4jStatementWithResource>
}

interface Neo4jStatementWithResourceRepositoryCustom : IdentityGenerator<StatementId>

class Neo4jStatementWithResourceRepositoryCustomImpl : Neo4jStatementWithResourceRepositoryCustom {
    var counter = 0L

    override fun nextIdentity(): StatementId {
        counter += 2
        return StatementId(counter)
    }
}
