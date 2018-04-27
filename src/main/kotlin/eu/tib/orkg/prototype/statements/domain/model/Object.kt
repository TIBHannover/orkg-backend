package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    Type(value = Object.Resource::class, name = "resource"),
    Type(value = Object.Literal::class, name = "literal")
)
sealed class Object {
    data class Resource(
        val id: ResourceId
    ) : Object()

    data class Literal(
        val value: String,
        val type: String = "string"
    ) : Object()
}
