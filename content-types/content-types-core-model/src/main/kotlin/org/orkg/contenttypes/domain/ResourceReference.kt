package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource

data class ResourceReference(
    val id: ThingId,
    val label: String,
    val classes: Set<ThingId>
) {
    constructor(resource: Resource) : this(resource.id, resource.label, resource.classes)
}
