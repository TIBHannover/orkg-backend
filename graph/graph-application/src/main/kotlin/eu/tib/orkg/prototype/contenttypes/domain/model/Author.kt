package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.statements.domain.model.ThingId

// TODO: convert to representation and merge with the one from statements package?
//       could use an abstraction of AuthorList (domain object) instead of List<Author> in Paper.
data class Author(
    val id: ThingId?,
    val name: String,
    val identifiers: Map<String, String>,
    val homepage: String?
)
