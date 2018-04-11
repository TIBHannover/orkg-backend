package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.core.Identity

data class SubjectId(override val value: String) :
    Identity<String>(value) {
    init {
        require(value.isNotEmpty()) { "Value cannot be empty" }
        require(value.isNotBlank()) { "Value cannot be blank" }
        require(value.isAlphaNumeric()) { "Value needs to be alpha-numeric" }
    }

    private fun String.isAlphaNumeric() =
        this.matches("""^[0-9a-fA-F]+$""".toRegex())
}
