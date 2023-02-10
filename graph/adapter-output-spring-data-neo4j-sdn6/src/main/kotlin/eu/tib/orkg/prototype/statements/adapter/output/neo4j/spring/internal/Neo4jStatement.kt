package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.RelationshipId
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode

@RelationshipProperties
class Neo4jStatement {
    @RelationshipId
    var id: Long? = null

    @Property("statement_id")
    var statementId: StatementId? = null

//    @TargetNode
    var subject: Neo4jThing? = null

    @TargetNode
    var `object`: Neo4jThing? = null

    @Property("predicate_id")
    var predicateId: PredicateId? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null
//
//    override fun toString(): String {
//        return "{id:$statementId}==(${subject!!.thingId} {${subject!!.label}})-[$predicateId]->(${`object`!!.thingId} {${`object`!!.label}})=="
//    }
}
