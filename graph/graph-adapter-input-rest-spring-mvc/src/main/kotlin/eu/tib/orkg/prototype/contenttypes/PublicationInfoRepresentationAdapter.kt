package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.api.PublicationInfoRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo

interface PublicationInfoRepresentationAdapter {
    fun PublicationInfo.toPublicationInfoRepresentation() : PublicationInfoRepresentation =
        PublicationInfoRepresentation(publishedMonth, publishedYear, publishedIn, url)
}
