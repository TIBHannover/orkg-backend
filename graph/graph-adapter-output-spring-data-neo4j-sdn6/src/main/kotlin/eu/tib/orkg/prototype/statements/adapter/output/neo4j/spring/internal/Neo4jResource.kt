package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.DynamicLabels
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

private val ReservedClassIds = setOf(
    ThingId("Literal"),
    ThingId("Class"),
    ThingId("Predicate"),
    ThingId("Resource")
)

@Node("Resource")
class Neo4jResource : Neo4jThing() {
    @Property("observatory_id")
    var observatory_id: ObservatoryId = ObservatoryId.createUnknownObservatory()

    @Property("extraction_method")
    var extraction_method: ExtractionMethod = ExtractionMethod.UNKNOWN

    @Property("verified")
    var verified: Boolean? = null

    @Property("visibility")
    var visibility: Visibility? = null

    @Property("unlisted_by")
    var unlisted_by: ContributorId? = null

    @Property("organization_id")
    var organization_id: OrganizationId = OrganizationId.createUnknownOrganization()

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
        id = id!!,
        label = label!!,
        createdAt = created_at!!,
        classes = classes - ReservedClassIds,
        createdBy = created_by,
        observatoryId = observatory_id,
        extractionMethod = extraction_method,
        organizationId = organization_id,
        visibility = visibility!!,
        verified = verified,
        unlistedBy = unlisted_by
    )

    override fun toThing() = toResource()
}
