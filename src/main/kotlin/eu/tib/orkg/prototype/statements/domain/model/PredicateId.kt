package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.statements.application.json.PredicateIdDeserializer
import eu.tib.orkg.prototype.statements.application.json.PredicateIdSerializer

@JsonDeserialize(using = PredicateIdDeserializer::class)
@JsonSerialize(using = PredicateIdSerializer::class)
data class PredicateId(private val value: String) {
    init {
        require(value.isNotEmpty()) { "Value cannot be empty" }
        require(value.isNotBlank()) { "Value cannot be blank" }
        require(value.startsWith("P")) { """Value needs to start with "P"""" }
        require(value.isAlphaNumericAfterPrefix()) { """Value starts with "P" but is not alpha-numeric afterwards""" }
    }

    override fun toString(): String {
        return value
    }

    private fun String.isAlphaNumericAfterPrefix(): Boolean {
        return this.matches("""^P([0-9a-fA-F])+$""".toRegex())
    }
}
