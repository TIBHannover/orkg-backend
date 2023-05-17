package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ObservatoryIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.OrganizationIdConverter
import java.time.OffsetDateTime
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.Labels
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

private val ReservedClassIds = setOf(
    ThingId("Literal"),
    ThingId("Class"),
    ThingId("Predicate"),
    ThingId("Resource")
)

@NodeEntity(label = "Resource")
data class Neo4jResource(
    @Id
    @GeneratedValue
    var nodeId: Long? = null
) : Neo4jThing {

    @Property("label")
    @Required
    override var label: String? = null

    @Property("id")
    @Required
    @Convert(ThingIdConverter::class)
    override var id: ThingId? = null

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    @Property("observatory_id")
    @Convert(ObservatoryIdConverter::class)
    var observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory()

    @Property("extraction_method")
    var extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN

    @Property("verified")
    var verified: Boolean? = null

    @Property("visibility")
    var visibility: Visibility? = null

    @Property("organization_id")
    @Convert(OrganizationIdConverter::class)
    var organizationId: OrganizationId = OrganizationId.createUnknownOrganization()

    /**
     * List of node labels. Labels other than the `Resource` label are mapped to classes.
     */
    @Labels
    private var labels: MutableList<String> = mutableListOf()

    /**
     * The list of classes that this node belongs to.
     */
    var classes: Set<ThingId>
        get() = labels.map { ThingId(it) }.toSet()
        set(value) {
            labels = value.map { it.value }.toMutableList()
        }

    constructor(
        label: String,
        resourceId: ThingId,
        createdBy: ContributorId = ContributorId.createUnknownContributor(),
        observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
        extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
        organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
        visibility: Visibility = Visibility.DEFAULT
    ) : this(null) {
        this.label = label
        this.id = resourceId
        this.createdBy = createdBy
        this.observatoryId = observatoryId
        this.extractionMethod = extractionMethod
        this.organizationId = organizationId
        this.visibility = visibility
    }

    fun toResource() =
        Resource(
            id = id!!,
            label = label!!,
            createdAt = createdAt!!,
            classes = classes - ReservedClassIds,
            createdBy = createdBy,
            observatoryId = observatoryId,
            extractionMethod = extractionMethod,
            organizationId = organizationId,
            visibility = visibility!!,
            verified = verified,
        )

    override fun toThing() = toResource()

    /**
     * Assign a class to this `Resource` node.
     */
    fun assignTo(clazz: String) = labels.add(clazz)
}
