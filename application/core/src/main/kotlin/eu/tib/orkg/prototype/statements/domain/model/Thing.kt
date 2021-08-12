package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore

sealed interface Thing {
    @get:JsonIgnore
    val thingId: String?
    val label: String
}
