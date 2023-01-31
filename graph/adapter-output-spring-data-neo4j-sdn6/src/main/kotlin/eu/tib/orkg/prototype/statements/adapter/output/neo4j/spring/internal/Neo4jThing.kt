package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.springframework.data.neo4j.core.schema.Node

@Node("Thing")
sealed interface Neo4jThing {
    val thingId: String?
    val label: String?

    fun toThing(): Thing
}
