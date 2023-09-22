package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.statements.domain.model.ThingId

data class ObjectIdAndLabel(
    val id: ThingId,
    val label: String
)
