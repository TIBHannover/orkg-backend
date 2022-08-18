package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.statements.application.json.ClassIdDeserializer
import eu.tib.orkg.prototype.statements.application.json.ClassIdSerializer

@JsonDeserialize(using = ClassIdDeserializer::class)
@JsonSerialize(using = ClassIdSerializer::class)
data class ClassId(val value: String) : Comparable<ClassId> {

    init {
        require(value.isNotBlank()) { "ID must not be blank" }
        require(value.matches(VALID_ID_REGEX)) { "Must only contain alphanumeric characters, dashes and underscores" }
    }

    constructor(value: Long) : this("C$value") {
        require(value >= 0) { "Value must be greater than or equal to zero" }
    }

    override fun toString() = value

    override fun compareTo(other: ClassId): Int = value.compareTo(other.value)
}

fun List<String>?.toClassIds(): Set<ClassId> = this?.map(::ClassId)?.toSet() ?: emptySet()
