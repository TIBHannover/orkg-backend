package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ClassReferenceRepresentation
import org.orkg.contenttypes.adapter.input.rest.LiteralReferenceRepresentation
import org.orkg.contenttypes.adapter.input.rest.PredicateReferenceRepresentation
import org.orkg.contenttypes.adapter.input.rest.ResourceReferenceRepresentation
import org.orkg.contenttypes.adapter.input.rest.ThingReferenceRepresentation
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.LiteralReference
import org.orkg.contenttypes.domain.PredicateReference
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.domain.ThingReference
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing

interface ThingReferenceRepresentationAdapter {
    fun ThingReference.toThingReferenceRepresentation(maskLiteralId: Boolean = true): ThingReferenceRepresentation =
        when (this) {
            is ResourceReference -> toResourceReferenceRepresentation()
            is PredicateReference -> toPredicateReferenceRepresentation()
            is ClassReference -> toClassReferenceRepresentation()
            is LiteralReference -> toLiteralReferenceRepresentation(maskLiteralId)
        }

    fun ResourceReference.toResourceReferenceRepresentation(): ResourceReferenceRepresentation =
        ResourceReferenceRepresentation(id, label, classes)

    fun PredicateReference.toPredicateReferenceRepresentation(): PredicateReferenceRepresentation =
        PredicateReferenceRepresentation(id, label)

    fun ClassReference.toClassReferenceRepresentation(): ClassReferenceRepresentation =
        ClassReferenceRepresentation(id, label, uri)

    fun LiteralReference.toLiteralReferenceRepresentation(maskId: Boolean = true): LiteralReferenceRepresentation =
        LiteralReferenceRepresentation(id.takeUnless { maskId }, label, datatype)

    fun Thing.toThingReferenceRepresentation(maskLiteralId: Boolean = true): ThingReferenceRepresentation =
        when (this) {
            is Resource -> toResourceReferenceRepresentation()
            is Predicate -> toPredicateReferenceRepresentation()
            is Class -> toClassReferenceRepresentation()
            is Literal -> toLiteralReferenceRepresentation(maskLiteralId)
        }

    fun Resource.toResourceReferenceRepresentation(): ResourceReferenceRepresentation =
        ResourceReferenceRepresentation(id, label, classes)

    fun Predicate.toPredicateReferenceRepresentation(): PredicateReferenceRepresentation =
        PredicateReferenceRepresentation(id, label)

    fun Class.toClassReferenceRepresentation(): ClassReferenceRepresentation =
        ClassReferenceRepresentation(id, label, uri)

    fun Literal.toLiteralReferenceRepresentation(maskId: Boolean = true): LiteralReferenceRepresentation =
        LiteralReferenceRepresentation(id.takeUnless { maskId }, label, datatype)
}
