package eu.tib.orkg.prototype.statements.domain.model

data class ClassId(val value: String) : Comparable<ClassId> {

    init {
        require(value.isNotBlank()) { "ID must not be blank" }
    }

    constructor(value: Long) : this("C$value") {
        require(value >= 0) { "Value must be greater than or equal to zero" }
    }

    override fun toString() = value

    override fun compareTo(other: ClassId): Int = value.compareTo(other.value)
}
