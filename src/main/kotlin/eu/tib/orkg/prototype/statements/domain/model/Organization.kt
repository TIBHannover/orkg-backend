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

    var observatories: Set<ObservatoryEntity>? = emptySet()
)
