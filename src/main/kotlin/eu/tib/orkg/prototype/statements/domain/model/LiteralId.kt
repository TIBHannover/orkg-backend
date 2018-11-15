package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.*
import eu.tib.orkg.prototype.statements.application.json.*

@JsonDeserialize(using = LiteralIdDeserializer::class)
@JsonSerialize(using = LiteralIdSerializer::class)
data class LiteralId(val value: Long) :
    Comparable<LiteralId> {

    init {
        require(value >= 0) { "Value must be greater than or equal to zero" }
    }

    @Deprecated("IDs of type String are no longer supported. Use Long instead.")
    constructor(value: String) : this(value.toLong())

    override fun toString() = "$value"

    override fun compareTo(other: LiteralId) = value.compareTo(other.value)
}
