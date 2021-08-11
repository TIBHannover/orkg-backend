package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node("Thing")
abstract class Neo4jThing : AuditableEntity() {
    @Id
    @GeneratedValue
    var id: Long? = null

    abstract val label: String?

    abstract val thingId: String?

    abstract fun toThing(): Thing
}
