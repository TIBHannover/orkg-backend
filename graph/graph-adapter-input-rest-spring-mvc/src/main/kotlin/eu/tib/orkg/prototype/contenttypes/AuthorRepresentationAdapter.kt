package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.application.AuthorRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI

interface AuthorRepresentationAdapter {

    fun List<Author>.mapToAuthorRepresentation() : List<AuthorRepresentation> =
        map { it.toAuthorRepresentation() }

    fun Author.toAuthorRepresentation() : AuthorRepresentation =
        object : AuthorRepresentation {
            override val id: ThingId? = this@toAuthorRepresentation.id
            override val name: String = this@toAuthorRepresentation.name
            override val identifiers: Map<String, String> = this@toAuthorRepresentation.identifiers.orEmpty()
            override val homepage: URI? = this@toAuthorRepresentation.homepage
        }
}
