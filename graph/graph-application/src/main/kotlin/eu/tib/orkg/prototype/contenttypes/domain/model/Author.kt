package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI

// TODO: convert to representation and merge with the one from statements package?
//       could use an abstraction of AuthorList (domain object) instead of List<Author> in Paper.
data class Author(
    val name: String,
    val id: ThingId? = null,
    val identifiers: Map<String, String>? = null,
    val homepage: URI? = null
)
