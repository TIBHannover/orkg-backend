package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literal
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Literal")
@Suppress("ktlint:standard:property-naming")
class Neo4jLiteral : Neo4jThing() {
    @Property("modifiable")
    var modifiable: Boolean? = null

    @Property("datatype")
    var datatype: String? = "xsd:string"

    @Property("extraction_method")
    var extraction_method: ExtractionMethod = ExtractionMethod.UNKNOWN

    fun toLiteral() =
        Literal(
            id = id!!,
            label = label!!,
            datatype = datatype!!,
            createdAt = created_at!!,
            createdBy = created_by,
            extractionMethod = extraction_method,
            modifiable = modifiable!!,
        )

    override fun toThing() = toLiteral()
}
