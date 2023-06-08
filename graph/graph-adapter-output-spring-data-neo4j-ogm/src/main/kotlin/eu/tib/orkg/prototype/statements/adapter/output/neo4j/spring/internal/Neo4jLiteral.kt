package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import java.time.OffsetDateTime
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
    var nodeId: Long? = null
) : Neo4jThing {
    @Property("label")
    @Required
    override var label: String? = null

    @Property("datatype")
    var datatype: String? = "xsd:string"

    @Property("id")
    @Required
    @Convert(ThingIdConverter::class)
    override var id: ThingId? = null

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    @Relationship(type = "HAS_VALUE_OF")
    @JsonIgnore
    var resources: MutableSet<Neo4jStatement> = mutableSetOf()

    @JsonIgnore
    private var labels: MutableList<String> = mutableListOf()

    val classes: Set<ThingId>
        get() = labels.map(::ThingId).toSet()

    constructor(
        label: String,
        id: ThingId,
        datatype: String = "xsd:string",
        createdBy: ContributorId = ContributorId.createUnknownContributor()
    ) : this(null) {
        this.label = label
        this.id = id
        this.datatype = datatype
        this.createdBy = createdBy
    }

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
