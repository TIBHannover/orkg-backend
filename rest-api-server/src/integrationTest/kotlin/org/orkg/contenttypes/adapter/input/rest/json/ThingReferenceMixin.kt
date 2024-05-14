package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.LiteralReference
import org.orkg.contenttypes.domain.PredicateReference
import org.orkg.contenttypes.domain.ResourceReference

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(value = [
    JsonSubTypes.Type(ResourceReference::class),
    JsonSubTypes.Type(LiteralReference::class),
    JsonSubTypes.Type(PredicateReference::class),
    JsonSubTypes.Type(ClassReference::class)
])
class ThingReferenceMixin
