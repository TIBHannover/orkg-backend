package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime

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
    val label: String = "",
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    val classes: Set<ClassId> = emptySet(),
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.createUnknownContributor()
)
