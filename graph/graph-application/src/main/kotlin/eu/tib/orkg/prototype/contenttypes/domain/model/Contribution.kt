package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility

data class Contribution(
    val id: ThingId,
    val label: String,
    val properties: Map<ThingId, List<ThingId>>,
    val visibility: Visibility
)
