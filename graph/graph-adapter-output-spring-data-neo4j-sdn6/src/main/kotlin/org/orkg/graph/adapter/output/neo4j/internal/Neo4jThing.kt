package org.orkg.graph.adapter.output.neo4j.internal

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Thing")
abstract class Neo4jThing {
    // We need this property for the ThingRepository
    @Id
    @Property("id")
    var id: ThingId? = null

    /**
     * This property should not be modified. It is purely used for optimistic locking
     * and increments automatically whenever the thing gets saved.
     */
    @Version
    @Deprecated("This property is only used for optimistic locking.")
    var version: Long? = null

    @Property("label")
    var label: String? = null

    @Property("created_by")
    var created_by: ContributorId = ContributorId.UNKNOWN

    @Property("created_at")
    var created_at: OffsetDateTime? = null

    abstract fun toThing(): Thing
}
