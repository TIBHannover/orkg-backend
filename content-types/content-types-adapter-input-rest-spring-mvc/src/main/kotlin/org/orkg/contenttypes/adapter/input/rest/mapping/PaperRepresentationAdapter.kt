package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.PaperRepresentation
import org.orkg.contenttypes.domain.Paper
import org.springframework.data.domain.Page

interface PaperRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<Paper>.mapToPaperRepresentation(): Optional<PaperRepresentation> =
        map { it.toPaperRepresentation() }

    fun Page<Paper>.mapToPaperRepresentation(): Page<PaperRepresentation> =
        map { it.toPaperRepresentation() }

    fun Paper.toPaperRepresentation(): PaperRepresentation =
        PaperRepresentation(
            id = id,
            title = title,
            researchFields = researchFields.mapToLabeledObjectRepresentation(),
            identifiers = identifiers,
            publicationInfo = publicationInfo.toPublicationInfoRepresentation(),
            authors = authors.mapToAuthorRepresentation(),
            contributions = contributions.mapToLabeledObjectRepresentation(),
            observatories = observatories,
            organizations = organizations,
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            verified = verified,
            visibility = visibility,
            modifiable = modifiable,
            unlistedBy = unlistedBy
        )
}
