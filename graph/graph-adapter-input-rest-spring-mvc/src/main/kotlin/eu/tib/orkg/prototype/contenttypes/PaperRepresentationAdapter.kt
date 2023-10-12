package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.api.PaperRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import java.util.*
import org.springframework.data.domain.Page

interface PaperRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<Paper>.mapToPaperRepresentation() : Optional<PaperRepresentation> =
        map { it.toPaperRepresentation() }

    fun Page<Paper>.mapToPaperRepresentation() : Page<PaperRepresentation> =
        map { it.toPaperRepresentation() }

    fun Paper.toPaperRepresentation() : PaperRepresentation =
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
            visibility = visibility
        )
}
