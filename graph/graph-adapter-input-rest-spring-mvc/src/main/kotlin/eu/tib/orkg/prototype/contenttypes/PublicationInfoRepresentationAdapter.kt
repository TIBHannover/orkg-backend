package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.application.PublicationInfoRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo

interface PublicationInfoRepresentationAdapter {
    fun PublicationInfo.toPublicationInfoRepresentation() : PublicationInfoRepresentation =
        object : PublicationInfoRepresentation {
            override val publishedMonth: Int? = this@toPublicationInfoRepresentation.publishedMonth
            override val publishedYear: Long? = this@toPublicationInfoRepresentation.publishedYear
            override val publishedIn: String? = this@toPublicationInfoRepresentation.publishedIn
            override val url: String? = this@toPublicationInfoRepresentation.url
        }
}
