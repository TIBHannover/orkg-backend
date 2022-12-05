package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity(label = "Thing")
sealed interface Neo4jThing {
    val thingId: String?
    val label: String?

    fun toThing(): Thing
}
