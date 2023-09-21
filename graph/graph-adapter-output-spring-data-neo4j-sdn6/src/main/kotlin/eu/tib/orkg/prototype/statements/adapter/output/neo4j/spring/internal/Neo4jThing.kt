package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Thing")
abstract class Neo4jThing {
    // We need this property for the ThingRepository
    @Id
    @GeneratedValue
    var nodeId: Long? = null

    @Property("id")
    var id: ThingId? = null

    @Property("label")
    var label: String? = null

    abstract fun toThing(): Thing
}