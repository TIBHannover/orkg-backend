package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.statements.application.json.ResourceIdDeserializer
import eu.tib.orkg.prototype.statements.application.json.ResourceIdSerializer

@JsonDeserialize(using = ResourceIdDeserializer::class)
@JsonSerialize(using = ResourceIdSerializer::class)
data class ResourceId(private val value: String) :
    Comparable<ResourceId> {
    init {
        require(value.isNotEmpty()) { "Value cannot be empty" }
        require(value.isNotBlank()) { "Value cannot be blank" }
        require(value.isAlphaNumeric()) { "Value needs to be alpha-numeric" }
    }

    override fun toString(): String {
        return value
    }

    private fun String.isAlphaNumeric() =
        this.matches("""^[0-9a-fA-F]+$""".toRegex())

    override fun compareTo(other: ResourceId) =
        value.compareTo(other.value)
}
