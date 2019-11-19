package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.Optional

interface Neo4jStatementWithResourceRepository :
    Neo4jRepository<Neo4jStatementWithResource, Long> {

    override fun findAll(): Iterable<Neo4jStatementWithResource>

    override fun findById(id: Long): Optional<Neo4jStatementWithResource>

    fun findByStatementId(id: StatementId): Optional<Neo4jStatementWithResource>

    // TODO: Return type as Slice not Page because Slice don't need a count query which might be more efficient
    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE sub.`resource_id`={0} RETURN rel, sub, obj, rel.statement_id AS id")
    fun findAllBySubject(subjectId: ResourceId, pagination: Pageable): Slice<Neo4jStatementWithResource>

    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE sub.`resource_id`={0} AND rel.predicate_id={1} RETURN rel, sub, obj, rel.statement_id AS id")
    fun findAllBySubjectAndPredicate(
        resourceId: ResourceId,
        predicateId: PredicateId,
        pagination: Pageable
    ): Slice<Neo4jStatementWithResource>

    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Slice<Neo4jStatementWithResource>

    @Query("MATCH (sub:`Resource`)-[rel:`RELATES_TO`]->(obj:`Resource`) WHERE obj.`resource_id`={0} RETURN rel, sub, obj, rel.statement_id AS id")
    fun findAllByObject(resourceId: ResourceId, pagination: Pageable): Slice<Neo4jStatementWithResource>

    @Query(
        "MATCH (s:Resource)-[p:RELATES_TO]->(o:Resource) RETURN " +
            "s.resource_id as subjectId, " +
            "p.predicate_id as predicateId, " +
            "o.resource_id as objectId"
    )
    fun listByIds(): List<IdTriple>
}

/**
 * A projection that contains the IDs of a statement for further conversion to RDF.
 */
// This class needs to be in the same package, as entity scanning will not work correctly otherwise.
@QueryResult
data class IdTriple(
    var subjectId: String? = null,
    var predicateId: String? = null,
    var objectId: String? = null
) {
    /**
     * Convert the triple to a statement in NTriple format.
     */
    fun toNTriple(): String {
        val rPrefix = RdfConstants.RESOURCE_NS
        val pPrefix = RdfConstants.PREDICATE_NS
        return "<$rPrefix$subjectId> <$pPrefix$predicateId> <$rPrefix$objectId> ."
    }
}
