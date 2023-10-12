package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.api.AuthorRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Author

interface AuthorRepresentationAdapter {

    fun List<Author>.mapToAuthorRepresentation() : List<AuthorRepresentation> =
        map { it.toAuthorRepresentation() }

    fun Author.toAuthorRepresentation() : AuthorRepresentation =
        AuthorRepresentation(id, name, identifiers.orEmpty(), homepage)
}
