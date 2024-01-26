package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
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
    var observatory_id: ObservatoryId = ObservatoryId.UNKNOWN

    @Property("extraction_method")
    var extraction_method: ExtractionMethod = ExtractionMethod.UNKNOWN

    @Property("verified")
    var verified: Boolean? = null

    @Property("visibility")
    var visibility: Visibility? = null

    @Property("unlisted_by")
    var unlisted_by: ContributorId? = null

    @Property("organization_id")
    var organization_id: OrganizationId = OrganizationId.UNKNOWN

    @Property("modifiable")
    var modifiable: Boolean? = null

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
        unlistedBy = unlisted_by,
        modifiable = modifiable!!
    )

    override fun toThing() = toResource()
}
