package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.core.Identity

data class PredicateId(override val value: String) :
    Identity<String>(value) {
    init {
        require(value.isNotEmpty()) { "Value cannot be empty" }
        require(value.isNotBlank()) { "Value cannot be blank" }
        require(value.startsWith("P")) { """Value needs to start with "P"""" }
        require(value.isAlphaNumericAfterPrefix()) { """Value starts with "P" but is not alpha-numeric afterwards""" }
    }

    private fun String.isAlphaNumericAfterPrefix(): Boolean {
        return this.matches("""^P([0-9a-fA-F])+$""".toRegex())
    }
}
