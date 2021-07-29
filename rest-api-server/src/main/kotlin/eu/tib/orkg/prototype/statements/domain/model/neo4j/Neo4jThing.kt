package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity(label = "Thing")
interface Neo4jThing {
    val thingId: String?
    val label: String?

    fun toThing(): Thing
}
