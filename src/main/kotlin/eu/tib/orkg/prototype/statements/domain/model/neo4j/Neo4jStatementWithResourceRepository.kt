package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.annotation.*
import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jStatementWithResourceRepository :
    Neo4jRepository<Neo4jStatementWithResource, Long> {

    override fun findAll(): Iterable<Neo4jStatementWithResource>

    override fun findById(id: Long): Optional<Neo4jStatementWithResource>

    @Query("MATCH (s:Resource)-[rel:RELATES_TO]->(:Resource) WHERE id(s)={0} RETURN rel")
    fun findAllBySubject(subjectId: Long): Iterable<Neo4jStatementWithResource>

    @Query("MATCH (s:Resource)-[rel:RELATES_TO]->(:Resource) WHERE id(s) = {0} AND rel.predicate_id={1} RETURN rel")
    fun findAllBySubjectAndPredicate(
        resourceId: Long,
        predicateId: Long
    ): Iterable<Neo4jStatementWithResource>

    fun findAllByPredicateId(predicateId: Long): Iterable<Neo4jStatementWithResource>
}
