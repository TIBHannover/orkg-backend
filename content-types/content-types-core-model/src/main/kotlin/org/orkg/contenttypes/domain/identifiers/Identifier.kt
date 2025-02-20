package org.orkg.contenttypes.domain.identifiers

import org.orkg.common.ThingId

data class Identifier(
    val id: String,
    val predicateId: ThingId,
    val newInstance: (String) -> IdentifierValue,
)
