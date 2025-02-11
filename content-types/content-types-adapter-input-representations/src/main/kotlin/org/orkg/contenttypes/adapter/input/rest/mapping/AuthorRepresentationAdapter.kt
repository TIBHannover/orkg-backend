package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.AuthorRepresentation
import org.orkg.contenttypes.domain.Author

interface AuthorRepresentationAdapter {

    fun List<Author>.mapToAuthorRepresentation(): List<AuthorRepresentation> =
        map { it.toAuthorRepresentation() }

    fun Author.toAuthorRepresentation(): AuthorRepresentation =
        AuthorRepresentation(id, name, identifiers.orEmpty(), homepage)
}
