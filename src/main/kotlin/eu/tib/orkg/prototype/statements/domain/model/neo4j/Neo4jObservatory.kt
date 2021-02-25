package eu.tib.orkg.prototype.statements.domain.model.neo4j
import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ObservatoryIdConverter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Observatory")
data class Neo4jObservatory(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Property("observatory_id")
    @Convert(ObservatoryIdConverter::class)
    var observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),

    @Property("name")
    var name: String? = null,

    @Property("description")
    var description: String? = null,

    @Property(name = "research_field")
    var researchField: String? = null,

    @Relationship(type = "BELONGS_TO", direction = Relationship.INCOMING)
    @JsonIgnore
    var organizations: MutableSet<Neo4jOrganization>? = mutableSetOf()

) {
    fun toObservatory() =
        Observatory(
            id = observatoryId,
            name = name,
            description = description,
            researchField = ResearchField(researchField, null),
            members = emptySet(),
            organizationIds = organizations!!.map { it.organizationId }.toSet()
        )
}
