package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.graph.domain.Literal
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Literal")
class Neo4jLiteral : Neo4jThing() {
    @Property("datatype")
    var datatype: String? = "xsd:string"

    fun toLiteral() =
        Literal(
            id = id!!,
            label = label!!,
            datatype = datatype!!,
            createdAt = created_at!!,
            createdBy = created_by
        )

    override fun toThing() = toLiteral()
}
