package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.statements.application.json.LiteralIdDeserializer
import eu.tib.orkg.prototype.statements.application.json.LiteralIdSerializer

@JsonDeserialize(using = LiteralIdDeserializer::class)
@JsonSerialize(using = LiteralIdSerializer::class)
data class LiteralId(val value: String) : Comparable<LiteralId> {

    init {
        require(value.isNotBlank()) { "ID must not be blank" }
        require(value.matches(VALID_ID_REGEX)) { "Must only contain alphanumeric characters, dashes and underscores" }
    }

    constructor(value: Long) : this("L$value") {
        require(value >= 0) { "Value must be greater than or equal to zero" }
    }

    override fun toString() = value

    override fun compareTo(other: LiteralId) = value.compareTo(other.value)
}
