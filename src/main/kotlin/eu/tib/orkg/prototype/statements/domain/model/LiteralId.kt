package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.*
import eu.tib.orkg.prototype.statements.application.json.*

@JsonDeserialize(using = LiteralIdDeserializer::class)
@JsonSerialize(using = LiteralIdSerializer::class)
data class LiteralId(val value: String) : Comparable<LiteralId> {

    init {
        require(value.isNotBlank()) { "ID must not be blank" }
        require(value.startsWith("L")) { "ID must start with \"L\"" }
    }

    constructor(value: Long) : this("L$value") {
        require(value >= 0) { "Value must be greater than or equal to zero" }
    }

    override fun toString() = value

    override fun compareTo(other: LiteralId) = value.compareTo(other.value)
}
