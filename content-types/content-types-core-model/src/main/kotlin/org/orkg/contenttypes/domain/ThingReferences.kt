package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource

sealed interface ThingReference {
    val id: ThingId
    val label: String
}

data class ResourceReference(
    override val id: ThingId,
    override val label: String,
    val classes: Set<ThingId>
) : ThingReference {
    constructor(resource: Resource) : this(resource.id, resource.label, resource.classes)
}

data class PredicateReference(
    override val id: ThingId,
    override val label: String,
    val description: String?
) : ThingReference {
    constructor(resource: Predicate) : this(resource.id, resource.label, resource.description)
}
