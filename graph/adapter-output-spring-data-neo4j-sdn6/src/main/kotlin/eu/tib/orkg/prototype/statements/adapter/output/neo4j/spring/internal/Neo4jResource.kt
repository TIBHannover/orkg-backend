package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

private val ReservedClassIds = setOf(
    ThingId("Literal"),
    ThingId("Class"),
    ThingId("Predicate"),
    ThingId("Resource")
)

@Node("Resource")
class Neo4jResource: Neo4jThing {
    @Id
    @Property("resource_id")
    var resourceId: ResourceId? = null

    @Property("label")
    override var label: String? = null

//    @Relationship(type = "RELATED")
//    var resources: MutableSet<Neo4jStatement> = mutableSetOf()

//    @Relationship(type = "RELATED", direction = Relationship.INCOMING)
//    var objectOf: MutableSet<Neo4jStatement> = mutableSetOf()

    @Property("created_by")
    @ConvertWith(converter = ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    @Property("observatory_id")
    var observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory()

    @Property("extraction_method")
    var extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN

    @Property("verified")
    var verified: Boolean? = null

    @Property("featured")
    var featured: Boolean? = null

    @Property("unlisted")
    var unlisted: Boolean? = null

    @Property("organization_id")
    var organizationId: OrganizationId = OrganizationId.createUnknownOrganization()

    /**
     * List of node labels. Labels other than the `Resource` label are mapped to classes.
     */
    @DynamicLabels
    private var labels: MutableSet<String> = mutableSetOf()

    /**
     * The list of classes that this node belongs to.
     */
    var classes: Set<ThingId>
        get() = labels.map { ThingId(it) }.toSet()
        set(value) {
            labels = value.map { it.value }.toMutableSet()
        }

    fun toResource() = Resource(
        id = resourceId,
        label = label!!,
        createdAt = createdAt!!,
        classes = classes - ReservedClassIds,
        createdBy = createdBy,
        observatoryId = observatoryId,
        extractionMethod = extractionMethod,
        organizationId = organizationId,
        featured = featured,
        unlisted = unlisted,
        verified = verified,
    )

    override val thingId: String?
        get() = resourceId?.value

    override fun toThing() = toResource()
}
