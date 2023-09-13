package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.RelationshipId
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode

/**
 * This class cannot be used to save and retrieve statements. It's only purpose is to serve as
 * a data object when retrieving delegated descriptions for [Neo4jClass]es [Neo4jPredicate]es.
 */
@RelationshipProperties
class Neo4jStatement {
    @RelationshipId
    var relationId: Long? = null

    @Property("statement_id")
    var id: StatementId? = null

    @Property("predicate_id")
    var predicateId: ThingId? = null

    @TargetNode
    var targetNode: Neo4jThing? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null
}
