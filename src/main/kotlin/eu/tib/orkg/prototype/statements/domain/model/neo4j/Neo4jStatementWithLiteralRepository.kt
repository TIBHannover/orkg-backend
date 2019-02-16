package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.data.neo4j.annotation.*
import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jStatementWithLiteralRepository :
    Neo4jRepository<Neo4jStatementWithLiteral, Long> {

    override fun findAll(depth: Int): Iterable<Neo4jStatementWithLiteral>

    override fun findById(id: Long): Optional<Neo4jStatementWithLiteral>

    @Query("MATCH (s:Resource)-[rel:HAS_VALUE_OF]->(:Literal) WHERE s.`resource_id`={0} RETURN rel")
    fun findAllBySubject(subjectId: ResourceId): Iterable<Neo4jStatementWithLiteral>

    @Query("MATCH (s:Resource)-[rel:HAS_VALUE_OF]->(:Literal) WHERE s.`resource_id`={0} AND rel.`predicate_id`={1} RETURN rel")
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId
    ): Iterable<Neo4jStatementWithLiteral>

    fun findAllByPredicateId(predicateId: PredicateId): Iterable<Neo4jStatementWithLiteral>
}
