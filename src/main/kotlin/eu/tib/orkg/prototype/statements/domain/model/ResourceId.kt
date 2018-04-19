package eu.tib.orkg.prototype.statements.domain.model

data class ResourceId(private val value: String) {
    init {
        require(value.isNotEmpty()) { "Value cannot be empty" }
        require(value.isNotBlank()) { "Value cannot be blank" }
        require(value.isAlphaNumeric()) { "Value needs to be alpha-numeric" }
    }

    override fun toString(): String {
        return value
    }

    private fun String.isAlphaNumeric() =
        this.matches("""^[0-9a-fA-F]+$""".toRegex())
}
