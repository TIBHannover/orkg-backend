package eu.tib.orkg.prototype.contenttypes.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.net.URI
import java.time.OffsetDateTime

interface PaperRepresentation {
    val id: ThingId
    val title: String
    @get:JsonProperty("research_fields")
    val researchFields: List<LabeledObjectRepresentation>
    val identifiers: Map<String, String>
    @get:JsonProperty("publication_info")
    val publicationInfo: PublicationInfoRepresentation
    val authors: List<AuthorRepresentation>
    val contributions: List<LabeledObjectRepresentation>
    @get:JsonProperty("observatories")
    val observatories: List<ObservatoryId>
    @get:JsonProperty("organizations")
    val organizations: List<OrganizationId>
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
    val verified: Boolean
    val visibility: Visibility
}

interface PublicationInfoRepresentation {
    @get:JsonProperty("published_month")
    val publishedMonth: Int?
    @get:JsonProperty("published_year")
    val publishedYear: Long?
    @get:JsonProperty("published_in")
    val publishedIn: String?
    val url: URI?
}

interface AuthorRepresentation {
    val id: ThingId?
    val name: String
    val identifiers: Map<String, String>
    val homepage: URI?
}

interface LabeledObjectRepresentation {
    val id: ThingId
    val label: String
}

interface ContributionRepresentation {
    val id: ThingId
    val label: String
    val classes: Set<ThingId>
    val properties: Map<ThingId, List<ThingId>>
    val visibility: Visibility
}

interface ComparisonRepresentation {
    val id: ThingId
    val title: String
    val description: String?
    @get:JsonProperty("research_fields")
    val researchFields: List<LabeledObjectRepresentation>
    val identifiers: Map<String, String>
    @get:JsonProperty("publication_info")
    val publicationInfo: PublicationInfoRepresentation
    val authors: List<AuthorRepresentation>
    val contributions: List<LabeledObjectRepresentation>
    val visualizations: List<LabeledObjectRepresentation>
    @get:JsonProperty("related_figures")
    val relatedFigures: List<LabeledObjectRepresentation>
    @get:JsonProperty("related_resources")
    val relatedResources: List<LabeledObjectRepresentation>
    val references: List<String>
    val observatories: List<ObservatoryId>
    val organizations: List<OrganizationId>
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
    @get:JsonProperty("previous_version")
    val previousVersion: ThingId?
    @get:JsonProperty("is_anonymized")
    val isAnonymized: Boolean
    val visibility: Visibility
}

interface ComparisonRelatedResourceRepresentation {
    val id: ThingId
    val label: String
    val image: String?
    val url: String?
    val description: String?
}

interface ComparisonRelatedFigureRepresentation {
    val id: ThingId
    val label: String
    val image: String?
    val description: String?
}

interface VisualizationRepresentation {
    val id: ThingId
    val title: String
    val description: String?
    val authors: List<AuthorRepresentation>
    val observatories: List<ObservatoryId>
    val organizations: List<OrganizationId>
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
    val visibility: Visibility
}
