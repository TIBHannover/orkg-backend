package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import java.util.UUID

data class Organization(
    val id: UUID?,

    var name: String?,

    var logo: String?,

    @JsonProperty("created_by")
    val createdBy: UUID? = UUID(0, 0),

    var url: String?,

    // TODO: Do we want/need a members list, as with observatories?

    @Deprecated("""The set of observatories is cyclic and will be removed. Use "observatory_ids" instead.""")
    val observatories: Set<ObservatoryEntity>? = emptySet(),

    @JsonProperty("observatory_ids")
    val observatoryIds: Set<UUID> = emptySet()
)
