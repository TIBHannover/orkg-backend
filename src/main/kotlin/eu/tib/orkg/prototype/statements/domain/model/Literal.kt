package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class Literal(
    val id: LiteralId?,
    override val label: String,
    val datatype: String = "xsd:string",
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: UUID = UUID(0, 0),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "literal"
) : Thing
