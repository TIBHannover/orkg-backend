package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.AuthorRecordRepresentation
import org.orkg.contenttypes.domain.AuthorRecord
import org.springframework.data.domain.Page

interface AuthorRecordRepresentationAdapter {
    fun Page<AuthorRecord>.mapToAuthorRecordRepresentation(): Page<AuthorRecordRepresentation> =
        map { it.toAuthorRecordRepresentation() }

    fun AuthorRecord.toAuthorRecordRepresentation(): AuthorRecordRepresentation =
        AuthorRecordRepresentation(
            authorId = authorId,
            authorName = authorName,
            comparisonCount = comparisonCount,
            paperCount = paperCount,
            visualizationCount = visualizationCount,
            totalCount = totalCount,
        )
}
