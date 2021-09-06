package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.LiteralIdConverter
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship

@Node(primaryLabel = "Literal")
class Neo4jLiteral() : Neo4jThing() {
    @Property("label")
    override var label: String? = null

    @Property("datatype")
    var datatype: String? = "xsd:string"

    @Property("literal_id")
    var literalId: LiteralId? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Relationship(type = "HAS_VALUE_OF")
    @JsonIgnore
    var resources: MutableSet<Neo4jStatement> = mutableSetOf()

    @JsonIgnore
    private var labels: MutableList<String> = mutableListOf()

    val classes: Set<ClassId>
        get() = labels.map(::ClassId).toSet()

    constructor(
        label: String,
        literalId: LiteralId,
        datatype: String = "xsd:string",
        createdBy: ContributorId = ContributorId.createUnknownContributor()
    ) : this() {
        this.label = label
        this.literalId = literalId
        this.datatype = datatype
        this.createdBy = createdBy
    }

    fun toLiteral() =
        Literal(id = literalId, label = label!!, datatype = datatype!!, createdAt = createdAt!!, createdBy = createdBy)

    override val thingId: String?
        get() = literalId?.value

    override fun toThing() = toLiteral()
}
