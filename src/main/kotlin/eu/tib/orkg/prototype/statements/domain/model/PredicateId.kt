package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.*
import eu.tib.orkg.prototype.statements.application.json.*

@JsonDeserialize(using = PredicateIdDeserializer::class)
@JsonSerialize(using = PredicateIdSerializer::class)
data class PredicateId(val value: Long) :
    Comparable<PredicateId> {

    init {
        require(value >= 0) { "Value must be equal to or greater than zero" }
    }

    constructor(value: String) : this(
        if (value.startsWith("P"))
            value.substring(1).toLong()
        else throw IllegalArgumentException("Value must start with \"P\"")
    )

    override fun toString() = "P$value"

    override fun compareTo(other: PredicateId) =
        value.compareTo(other.value)
}
