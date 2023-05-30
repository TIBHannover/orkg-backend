package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.statements.domain.model.ThingId

data class Author(
    val id: ThingId?,
    val name: String,
    val identifiers: Map<String, String>,
    val homepage: String?
)
