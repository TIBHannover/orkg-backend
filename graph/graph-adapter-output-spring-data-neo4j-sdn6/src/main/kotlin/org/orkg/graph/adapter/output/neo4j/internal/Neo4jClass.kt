package org.orkg.graph.adapter.output.neo4j.internal

import java.net.URI
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicates
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship

@Node("Class")
class Neo4jClass : Neo4jThing() {
    @Property("uri")
    var uri: String? = null

    @Property("modifiable")
    var modifiable: Boolean? = null

    @Relationship(type = "RELATED", direction = Relationship.Direction.OUTGOING)
    var statements: MutableList<Neo4jStatement> = mutableListOf()

    fun toClass() = Class(
        id = id!!,
        label = label!!,
        uri = if (uri != null) URI.create(uri!!) else null,
        createdAt = created_at!!,
        createdBy = created_by,
        description = statements.singleOrNull { it.predicateId == Predicates.description }?.targetNode?.label,
        modifiable = modifiable!!
    )

    override fun toThing() = toClass()
}
