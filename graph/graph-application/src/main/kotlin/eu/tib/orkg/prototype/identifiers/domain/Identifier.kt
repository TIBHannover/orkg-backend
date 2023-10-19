package eu.tib.orkg.prototype.identifiers.domain

import eu.tib.orkg.prototype.statements.domain.model.ThingId

data class Identifier(
    val id: String,
    val predicateId: ThingId,
    val newInstance: (String) -> IdentifierValue
)
