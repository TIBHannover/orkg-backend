package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import java.time.LocalDateTime
import java.time.OffsetDateTime

@JsonSubTypes(
    JsonSubTypes.Type(value = Thing.Resource::class, name = "resource"),
    JsonSubTypes.Type(value = Thing.Literal::class, name = "literal"),
    JsonSubTypes.Type(value = Thing.Predicate::class, name = "predicate"),
    JsonSubTypes.Type(value = Thing.Class::class, name = "class")
)
open class Thing {
    data class Resource(
        val id: ResourceId
    ) : Thing() {
        @JsonProperty("created_at")
        val createdAt: LocalDateTime = LocalDateTime.now()
    }

    data class Literal(
        val id: LiteralId
    ) : Thing() {
        @JsonProperty("created_at")
        val createdAt: OffsetDateTime = OffsetDateTime.now()
    }

    data class Predicate(
        val id: PredicateId
    ) : Thing() {
        @JsonProperty("created_at")
        val createdAt: OffsetDateTime = OffsetDateTime.now()
    }

    data class Class(
        val id: ClassId
    ) : Thing() {
        @JsonProperty("created_at")
        val createdAt: OffsetDateTime = OffsetDateTime.now()
    }
}
