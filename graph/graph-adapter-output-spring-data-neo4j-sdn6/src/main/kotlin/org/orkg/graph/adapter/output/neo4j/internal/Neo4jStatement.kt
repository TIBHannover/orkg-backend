package org.orkg.graph.adapter.output.neo4j.internal

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.StatementId
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
    var createdBy: ContributorId = ContributorId.UNKNOWN

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    @Property("index")
    var index: Int? = null

    @Property("modifiable")
    var modifiable: Boolean = true
}
