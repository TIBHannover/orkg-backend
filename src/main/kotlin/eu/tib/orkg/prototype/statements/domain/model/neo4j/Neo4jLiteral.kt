package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.LiteralIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.UUIDGraphAttributeConverter
import java.util.UUID
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Literal")
data class Neo4jLiteral(
    @Id
    @GeneratedValue
    var id: Long? = null
) : Neo4jThing, AuditableEntity() {
    @Property("label")
    @Required
    override var label: String? = null

    @Property("datatype")
    var datatype: String? = "xsd:string"

    @Property("literal_id")
    @Required
    @Convert(LiteralIdGraphAttributeConverter::class)
    var literalId: LiteralId? = null

    @Property("created_by")
    @Convert(UUIDGraphAttributeConverter::class)
    var createdBy: UUID = UUID(0, 0)

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
        createdBy: UUID = UUID(0, 0)
    ) : this(null) {
        this.label = label
        this.literalId = literalId
        this.datatype = datatype
        this.createdBy = createdBy
    }

    fun toLiteral() =
        Literal(id = literalId, label = label!!, datatype = datatype!!, createdAt = createdAt!!, createdBy = ContributorId(createdBy))

    override val thingId: String?
        get() = literalId?.value

    override fun toThing() = toLiteral()
}
