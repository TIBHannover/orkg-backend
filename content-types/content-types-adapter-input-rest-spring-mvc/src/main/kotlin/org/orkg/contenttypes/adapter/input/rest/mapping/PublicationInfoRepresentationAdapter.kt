package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.PublicationInfoRepresentation
import org.orkg.contenttypes.domain.PublicationInfo

interface PublicationInfoRepresentationAdapter {
    fun PublicationInfo.toPublicationInfoRepresentation() : PublicationInfoRepresentation =
        PublicationInfoRepresentation(publishedMonth, publishedYear, publishedIn, url)
}
