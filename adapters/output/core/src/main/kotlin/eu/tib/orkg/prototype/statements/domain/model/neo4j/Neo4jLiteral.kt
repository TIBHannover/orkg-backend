package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.OffsetDateTimeConverter
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import java.time.OffsetDateTime

@Node(primaryLabel = "Literal")
class Neo4jLiteral(
    @Property("label")
    override var label: String? = null,

    @Property("datatype")
    var datatype: String? = "xsd:string",

    @Property("literal_id")
    var literalId: LiteralId? = null,

    @Property("created_at")
    @ConvertWith(converter = OffsetDateTimeConverter::class)
    var createdAt: OffsetDateTime? = OffsetDateTime.now(),

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()
) : Neo4jThing() {

    fun toLiteral() =
        Literal(id = literalId, label = label!!, datatype = datatype!!, createdAt = createdAt!!, createdBy = createdBy)

    override val thingId: String?
        get() = literalId?.value

    override fun toThing() = toLiteral()
}
