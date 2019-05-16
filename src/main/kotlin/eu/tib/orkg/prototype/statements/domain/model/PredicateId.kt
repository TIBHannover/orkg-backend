package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.*
import eu.tib.orkg.prototype.statements.application.json.*

@JsonDeserialize(using = PredicateIdDeserializer::class)
@JsonSerialize(using = PredicateIdSerializer::class)
data class PredicateId(val value: String) :
    Comparable<PredicateId> {

    init {
        require(value.isNotBlank()) { "ID must not be blank" }
    }

    constructor(value: Long) : this("P$value") {
        require(value >= 0) { "ID must be greater than or equal to zero" }
    }

    override fun toString() = value

    override fun compareTo(other: PredicateId) =
        value.compareTo(other.value)
}
