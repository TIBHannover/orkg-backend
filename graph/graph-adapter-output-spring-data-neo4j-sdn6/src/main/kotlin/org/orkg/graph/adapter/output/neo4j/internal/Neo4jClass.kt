package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.toIRIOrNull
import org.orkg.graph.domain.Class
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Class")
class Neo4jClass : Neo4jThing() {
    @Property("uri")
    var uri: String? = null

    @Property("modifiable")
    var modifiable: Boolean? = null

    fun toClass() = Class(
        id = id!!,
        label = label!!,
        uri = uri?.toIRIOrNull(),
        createdAt = created_at!!,
        createdBy = created_by,
        modifiable = modifiable!!
    )

    override fun toThing() = toClass()
}
