package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "_class"
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = LiteralObject::class,
        name = "literal"
    )
)
data class LiteralObject(
    val id: LiteralId?,
    val label: String,
    val classes: Set<ClassId> = emptySet()
)
