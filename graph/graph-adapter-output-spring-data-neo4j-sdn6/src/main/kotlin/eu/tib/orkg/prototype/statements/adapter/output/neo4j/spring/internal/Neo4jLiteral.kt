package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.Literal
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
