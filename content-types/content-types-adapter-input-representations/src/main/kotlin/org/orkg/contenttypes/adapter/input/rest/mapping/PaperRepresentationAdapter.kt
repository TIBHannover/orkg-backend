package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.PaperRepresentation
import org.orkg.contenttypes.domain.Paper
import org.springframework.data.domain.Page
import java.util.Optional

interface PaperRepresentationAdapter :
    AuthorRepresentationAdapter,
    LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter,
    ThingReferenceRepresentationAdapter {
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
            sustainableDevelopmentGoals = sustainableDevelopmentGoals.mapToLabeledObjectRepresentation(),
            mentionings = mentionings.map { it.toResourceReferenceRepresentation() }.toSet(),
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
