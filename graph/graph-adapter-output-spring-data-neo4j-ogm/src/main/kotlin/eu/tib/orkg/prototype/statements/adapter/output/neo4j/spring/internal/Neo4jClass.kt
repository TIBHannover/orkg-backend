package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import java.net.URI
import java.time.OffsetDateTime
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Class")
data class Neo4jClass(
    @Id
    @GeneratedValue
    var nodeId: Long? = null,
    @Relationship(type = "RELATED", direction = Relationship.OUTGOING)
    @JsonIgnore
    var subjectOf: MutableSet<Neo4jStatement> = mutableSetOf()
) : Neo4jThing {
    @Property("id")
    @Required
    @Convert(ThingIdConverter::class)
    override var id: ThingId? = null

    @Property("label")
    @Required
    override var label: String? = null

    @Property("uri")
    var uri: String? = null

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    constructor(
        label: String,
        id: ThingId,
        createdBy: ContributorId = ContributorId.createUnknownContributor(),
        uri: URI?
    ) : this(null) {
        this.label = label
        this.id = id
        this.uri = uri?.toString()
        this.createdBy = createdBy
    }

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    fun toClass(): Class = Class(
        id = id!!,
        label = label!!,
        uri = if (uri != null) URI.create(uri!!) else null,
        createdAt = createdAt!!,
        createdBy = createdBy,
        description = subjectOf.singleOrNull { it.predicateId?.value == "description" }?.`object`?.label
    )

    override fun toThing() = toClass()
}
