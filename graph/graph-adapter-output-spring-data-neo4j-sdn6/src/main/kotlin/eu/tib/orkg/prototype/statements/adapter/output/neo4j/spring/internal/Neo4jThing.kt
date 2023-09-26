package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Thing")
abstract class Neo4jThing {
    // We need this property for the ThingRepository
    @Id
    @Property("id")
    var id: ThingId? = null

    @Property("label")
    var label: String? = null

    @Property("created_by")
    var created_by: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var created_at: OffsetDateTime? = null

    abstract fun toThing(): Thing
}
