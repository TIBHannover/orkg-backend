package org.orkg.contenttypes.domain

import java.net.URI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
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
    constructor(predicate: Predicate) : this(predicate.id, predicate.label, predicate.description)
}

data class ClassReference(
    override val id: ThingId,
    override val label: String,
    val uri: URI?
) : ThingReference {
    constructor(`class`: Class) : this(`class`.id, `class`.label, `class`.uri)
}
