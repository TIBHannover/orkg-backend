package org.orkg.contenttypes.domain

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing

sealed interface ThingReference {
    val id: ThingId?
    val label: String

    companion object {
        fun from(thing: Thing, maskLiteralId: Boolean = true): ThingReference =
            when (thing) {
                is Resource -> ResourceReference(thing)
                is Class -> ClassReference(thing)
                is Literal -> LiteralReference(thing, maskLiteralId)
                is Predicate -> PredicateReference(thing)
            }
    }
}

data class ResourceReference(
    override val id: ThingId,
    override val label: String,
    val classes: Set<ThingId>,
) : ThingReference {
    constructor(resource: Resource) : this(resource.id, resource.label, resource.classes)
}

data class PredicateReference(
    override val id: ThingId,
    override val label: String,
) : ThingReference {
    constructor(predicate: Predicate) : this(predicate.id, predicate.label)
}

data class ClassReference(
    override val id: ThingId,
    override val label: String,
    val uri: ParsedIRI?,
) : ThingReference {
    constructor(`class`: Class) : this(`class`.id, `class`.label, `class`.uri)
}

data class LiteralReference(
    override val id: ThingId?,
    override val label: String,
    val datatype: String,
) : ThingReference {
    constructor(literal: Literal, maskId: Boolean = true) :
        this(literal.id.takeUnless { maskId }, literal.label, literal.datatype)
}

inline val Set<ThingReference>.ids get() = mapNotNullTo(mutableSetOf()) { it.id }
