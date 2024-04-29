package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "_class"
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = ResourceObject::class,
        name = "resource"
    )
)
data class ResourceObject(
    val id: ThingId?,
    val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    val classes: Set<ThingId> = emptySet(),
    val shared: Int = 0,
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.UNKNOWN
)
