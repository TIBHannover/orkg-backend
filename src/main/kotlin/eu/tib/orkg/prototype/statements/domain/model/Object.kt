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
sealed class Object : Comparable<Object> {
    data class Resource(
        val id: ResourceId
    ) : Object() {
        override fun compareTo(other: Object): Int {
            // Literals are always sorted below resources
            return when (other) {
                is Resource -> id.compareTo(other.id)
                is Literal -> -1
            }
        }
    }

    data class Literal(
        val value: String
        // TODO: "type" is reserved by the serializer. needs solution.
        //var datatype: String? = "string"
    ) : Object() {
        override fun compareTo(other: Object): Int {
            // Resources are always sorted before resources
            return when (other) {
                is Resource -> 1
                is Literal -> value.compareTo(other.value)
            }
        }
    }
}
