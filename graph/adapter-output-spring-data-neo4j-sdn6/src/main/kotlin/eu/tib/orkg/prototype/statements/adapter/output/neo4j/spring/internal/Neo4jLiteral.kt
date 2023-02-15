package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Literal")
class Neo4jLiteral : Neo4jThing() {
    @Property("literal_id")
    var literalId: LiteralId? = null

    @Property("datatype")
    var datatype: String? = "xsd:string"

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    private var labels: MutableList<String> = mutableListOf()

    val classes: Set<ClassId>
        get() = labels.map(::ClassId).toSet()

    fun toLiteral() =
        Literal(
            id = literalId,
            label = label!!,
            datatype = datatype!!,
            createdAt = createdAt!!,
            createdBy = createdBy
        )

    override val thingId: String?
        get() = literalId?.value

    override fun toThing() = toLiteral()
}
