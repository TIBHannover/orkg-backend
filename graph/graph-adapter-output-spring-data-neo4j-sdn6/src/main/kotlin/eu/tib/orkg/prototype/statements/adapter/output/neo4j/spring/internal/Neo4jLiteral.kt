package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Literal")
class Neo4jLiteral : Neo4jThing() {
    @Property("datatype")
    var datatype: String? = "xsd:string"

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    private var labels: MutableList<String> = mutableListOf()

    val classes: Set<ThingId>
        get() = labels.map(::ThingId).toSet()

    fun toLiteral() =
        Literal(
            id = id!!,
            label = label!!,
            datatype = datatype!!,
            createdAt = createdAt!!,
            createdBy = createdBy
        )

    override fun toThing() = toLiteral()
}
