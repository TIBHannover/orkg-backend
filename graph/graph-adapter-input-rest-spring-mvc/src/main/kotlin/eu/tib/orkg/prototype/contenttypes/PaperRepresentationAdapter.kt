package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.application.AuthorRepresentation
import eu.tib.orkg.prototype.contenttypes.application.LabeledObjectRepresentation
import eu.tib.orkg.prototype.contenttypes.application.PaperRepresentation
import eu.tib.orkg.prototype.contenttypes.application.PublicationInfoRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface PaperRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<Paper>.mapToPaperRepresentation() : Optional<PaperRepresentation> =
        map { it.toPaperRepresentation() }

    fun Page<Paper>.mapToPaperRepresentation() : Page<PaperRepresentation> =
        map { it.toPaperRepresentation() }

    fun Paper.toPaperRepresentation() : PaperRepresentation =
        object : PaperRepresentation {
            override val id: ThingId = this@toPaperRepresentation.id
            override val title: String = this@toPaperRepresentation.title
            override val researchFields: List<LabeledObjectRepresentation> =
                this@toPaperRepresentation.researchFields.mapToLabeledObjectRepresentation()
            override val identifiers: Map<String, String> = this@toPaperRepresentation.identifiers
            override val publicationInfo: PublicationInfoRepresentation =
                this@toPaperRepresentation.publicationInfo.toPublicationInfoRepresentation()
            override val authors: List<AuthorRepresentation> =
                this@toPaperRepresentation.authors.mapToAuthorRepresentation()
            override val contributions: List<LabeledObjectRepresentation> =
                this@toPaperRepresentation.contributions.mapToLabeledObjectRepresentation()
            override val observatories: List<ObservatoryId> = this@toPaperRepresentation.observatories
            override val organizations: List<OrganizationId> = this@toPaperRepresentation.organizations
            override val extractionMethod: ExtractionMethod = this@toPaperRepresentation.extractionMethod
            override val createdAt: OffsetDateTime = this@toPaperRepresentation.createdAt
            override val createdBy: ContributorId = this@toPaperRepresentation.createdBy
            override val verified: Boolean = this@toPaperRepresentation.verified
            override val visibility: Visibility = this@toPaperRepresentation.visibility
        }
}
