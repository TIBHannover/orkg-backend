package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.*
import eu.tib.orkg.prototype.statements.application.json.*

@JsonDeserialize(using = StatementIdDeserializer::class)
@JsonSerialize(using = StatementIdSerializer::class)
data class StatementId(val value: String) : Comparable<StatementId> {

    init {
        require(value.isNotBlank()) { "ID must not be blank" }
        require(value.startsWith("S")) { "ID must start with \"S\"" }
    }

    constructor(value: Long) : this("S$value") {
        require(value >= 0) { "Value must be greater than or equal to zero" }
    }

    override fun toString() = value

    override fun compareTo(other: StatementId) = value.compareTo(other.value)
}
