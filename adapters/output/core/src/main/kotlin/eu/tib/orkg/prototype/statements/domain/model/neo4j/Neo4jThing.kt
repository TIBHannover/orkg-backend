package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.springframework.data.neo4j.core.schema.Node

@Node("Thing")
interface Neo4jThing {
    val thingId: String?
    val label: String?

    fun toThing(): Thing
}
