package eu.tib.orkg.prototype.statements.domain.model

data class Thing(val value: String) {
    init {
        require(value.isNotEmpty()) { "The value cannot be empty" }
        require(value.isNotBlank()) { "The value cannot be blank" }
    }
}

data class Relationship(val value: String) {
    init {
        require(value.isNotEmpty()) { "The value cannot be empty" }
        require(value.isNotBlank()) { "The value cannot be blank" }
    }
}

data class Statement(
    val sub: Thing,
    val rel: Relationship,
    val obj: Thing
)

interface StatementRepository {
}
