package org.orkg.graph.adapter.input.rest.json

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "_class"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = Predicate::class, name = "predicate"),
        JsonSubTypes.Type(value = Class::class, name = "class"),
        JsonSubTypes.Type(value = Literal::class, name = "literal"),
        JsonSubTypes.Type(value = Resource::class, name = "resource"),
    ]
)
abstract class ThingMixin
