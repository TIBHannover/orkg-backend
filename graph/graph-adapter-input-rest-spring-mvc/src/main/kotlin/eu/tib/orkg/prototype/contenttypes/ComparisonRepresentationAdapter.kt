package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.application.AuthorRepresentation
import eu.tib.orkg.prototype.contenttypes.application.ComparisonRepresentation
import eu.tib.orkg.prototype.contenttypes.application.LabeledObjectRepresentation
import eu.tib.orkg.prototype.contenttypes.application.PublicationInfoRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Comparison
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface ComparisonRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<Comparison>.mapToComparisonRepresentation() : Optional<ComparisonRepresentation> =
        map { it.toComparisonRepresentation() }

    fun Page<Comparison>.mapToComparisonRepresentation() : Page<ComparisonRepresentation> =
        map { it.toComparisonRepresentation() }

    fun Comparison.toComparisonRepresentation() : ComparisonRepresentation =
        object : ComparisonRepresentation {
            override val id: ThingId = this@toComparisonRepresentation.id
            override val title: String = this@toComparisonRepresentation.title
            override val description: String? = this@toComparisonRepresentation.description
            override val researchFields: List<LabeledObjectRepresentation> =
                this@toComparisonRepresentation.researchFields.mapToLabeledObjectRepresentation()
            override val identifiers: Map<String, String> = this@toComparisonRepresentation.identifiers
            override val publicationInfo: PublicationInfoRepresentation =
                this@toComparisonRepresentation.publicationInfo.toPublicationInfoRepresentation()
            override val authors: List<AuthorRepresentation> =
                this@toComparisonRepresentation.authors.mapToAuthorRepresentation()
            override val contributions: List<LabeledObjectRepresentation> =
                this@toComparisonRepresentation.contributions.mapToLabeledObjectRepresentation()
            override val visualizations: List<LabeledObjectRepresentation> =
                this@toComparisonRepresentation.visualizations.mapToLabeledObjectRepresentation()
            override val relatedFigures: List<LabeledObjectRepresentation> =
                this@toComparisonRepresentation.relatedFigures.mapToLabeledObjectRepresentation()
            override val relatedResources: List<LabeledObjectRepresentation> =
                this@toComparisonRepresentation.relatedResources.mapToLabeledObjectRepresentation()
            override val references: List<String> = this@toComparisonRepresentation.references
            override val observatories: List<ObservatoryId> = this@toComparisonRepresentation.observatories
            override val organizations: List<OrganizationId> = this@toComparisonRepresentation.organizations
            override val extractionMethod: ExtractionMethod = this@toComparisonRepresentation.extractionMethod
            override val createdAt: OffsetDateTime = this@toComparisonRepresentation.createdAt
            override val createdBy: ContributorId = this@toComparisonRepresentation.createdBy
            override val previousVersion: ThingId? = this@toComparisonRepresentation.previousVersion
            override val isAnonymized: Boolean = this@toComparisonRepresentation.isAnonymized
            override val visibility: Visibility = this@toComparisonRepresentation.visibility
        }
}
