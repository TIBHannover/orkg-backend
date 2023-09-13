package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.application.AuthorRepresentation
import eu.tib.orkg.prototype.contenttypes.application.VisualizationRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Visualization
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface VisualizationRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<Visualization>.mapToVisualizationRepresentation() : Optional<VisualizationRepresentation> =
        map { it.toVisualizationRepresentation() }

    fun Page<Visualization>.mapToVisualizationRepresentation() : Page<VisualizationRepresentation> =
        map { it.toVisualizationRepresentation() }

    fun Visualization.toVisualizationRepresentation() : VisualizationRepresentation =
        object : VisualizationRepresentation {
            override val id: ThingId = this@toVisualizationRepresentation.id
            override val title: String = this@toVisualizationRepresentation.title
            override val description: String? = this@toVisualizationRepresentation.description
            override val authors: List<AuthorRepresentation> =
                this@toVisualizationRepresentation.authors.mapToAuthorRepresentation()
            override val observatories: List<ObservatoryId> = this@toVisualizationRepresentation.observatories
            override val organizations: List<OrganizationId> = this@toVisualizationRepresentation.organizations
            override val extractionMethod: ExtractionMethod = this@toVisualizationRepresentation.extractionMethod
            override val createdAt: OffsetDateTime = this@toVisualizationRepresentation.createdAt
            override val createdBy: ContributorId = this@toVisualizationRepresentation.createdBy
            override val visibility: Visibility = this@toVisualizationRepresentation.visibility
        }
}
