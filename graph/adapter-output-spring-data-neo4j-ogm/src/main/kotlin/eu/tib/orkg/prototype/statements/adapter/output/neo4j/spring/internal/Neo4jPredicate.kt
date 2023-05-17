package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import java.time.OffsetDateTime
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Predicate")
data class Neo4jPredicate(
    @Id
    @GeneratedValue
    var nodeId: Long? = null,

    @Property("label")
    @Required
    override var label: String? = null,

    @Property("id")
    @Required
    @Convert(ThingIdConverter::class)
    override var id: ThingId? = null,

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor(),

    @Property("created_at")
    var createdAt: OffsetDateTime? = null,

    @Relationship(type = "RELATED", direction = Relationship.OUTGOING)
    @JsonIgnore
    var subjectOf: MutableSet<Neo4jStatement> = mutableSetOf()
) : Neo4jThing {

    fun toPredicate(): Predicate {
        val predicate = Predicate(
            id = id!!,
            label = label!!,
            createdAt = createdAt!!,
            createdBy = createdBy
        )
        if (subjectOf.isNotEmpty())
            predicate.description = subjectOf.firstOrNull { it.predicateId?.value == "description" }?.`object`?.label
        return predicate
    }

    override fun toThing() = toPredicate()
}
