package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.statements.application.json.ResourceIdDeserializer
import eu.tib.orkg.prototype.statements.application.json.ResourceIdSerializer

@Suppress("RegExpSimplifiable")
val VALID_ID_REGEX: Regex = """^[a-zA-Z0-9:_-]+$""".toRegex()

@JsonDeserialize(using = ResourceIdDeserializer::class)
@JsonSerialize(using = ResourceIdSerializer::class)
data class ResourceId(val value: String) :
    Comparable<ResourceId> {

    init {
        require(value.isNotBlank()) { "ID must not be blank" }
        require(value.matches(VALID_ID_REGEX)) { "Must only contain alphanumeric characters, colons, dashes and underscores" }
    }

    constructor(value: Long) : this("R$value") {
        require(value >= 0) { "ID must be greater than or equal to zero" }
    }

    override fun toString() = value

    override fun compareTo(other: ResourceId) =
        value.compareTo(other.value)
}
