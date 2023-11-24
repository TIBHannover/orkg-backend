package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.time.OffsetDateTime
import javax.validation.constraints.NotBlank
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

data class PaperRepresentation(
    val id: ThingId,
    val title: String,
    @get:JsonProperty("research_fields")
    val researchFields: List<LabeledObjectRepresentation>,
    val identifiers: Map<String, String>,
    @get:JsonProperty("publication_info")
    val publicationInfo: PublicationInfoRepresentation,
    val authors: List<AuthorRepresentation>,
    val contributions: List<LabeledObjectRepresentation>,
    @get:JsonProperty("observatories")
    val observatories: List<ObservatoryId>,
    @get:JsonProperty("organizations")
    val organizations: List<OrganizationId>,
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    val verified: Boolean,
    val visibility: Visibility
)

data class PublicationInfoRepresentation(
    @get:JsonProperty("published_month")
    val publishedMonth: Int?,
    @get:JsonProperty("published_year")
    val publishedYear: Long?,
    @get:JsonProperty("published_in")
    val publishedIn: String?,
    val url: URI?
)

data class AuthorRepresentation(
    val id: ThingId?,
    val name: String,
    val identifiers: Map<String, String>,
    val homepage: URI?
)

data class LabeledObjectRepresentation(
    val id: ThingId,
    val label: String
)

data class ContributionRepresentation(
    val id: ThingId,
    val label: String,
    val classes: Set<ThingId>,
    val properties: Map<ThingId, List<ThingId>>,
    val visibility: Visibility
)

data class ComparisonRepresentation(
    val id: ThingId,
    val title: String,
    val description: String?,
    @get:JsonProperty("research_fields")
    val researchFields: List<LabeledObjectRepresentation>,
    val identifiers: Map<String, String>,
    @get:JsonProperty("publication_info")
    val publicationInfo: PublicationInfoRepresentation,
    val authors: List<AuthorRepresentation>,
    val contributions: List<LabeledObjectRepresentation>,
    val visualizations: List<LabeledObjectRepresentation>,
    @get:JsonProperty("related_figures")
    val relatedFigures: List<LabeledObjectRepresentation>,
    @get:JsonProperty("related_resources")
    val relatedResources: List<LabeledObjectRepresentation>,
    val references: List<String>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    @get:JsonProperty("previous_version")
    val previousVersion: ThingId?,
    @get:JsonProperty("is_anonymized")
    val isAnonymized: Boolean,
    val visibility: Visibility
)

data class ComparisonRelatedResourceRepresentation(
    val id: ThingId,
    val label: String,
    val image: String?,
    val url: String?,
    val description: String?
)

data class ComparisonRelatedFigureRepresentation(
    val id: ThingId,
    val label: String,
    val image: String?,
    val description: String?
)

data class VisualizationRepresentation(
    val id: ThingId,
    val title: String,
    val description: String?,
    val authors: List<AuthorRepresentation>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    val visibility: Visibility
)

data class AuthorDTO(
    val id: ThingId?,
    @NotBlank
    val name: String,
    val identifiers: Map<String, String>?,
    val homepage: URI?
) {
    fun toCreateCommand(): Author =
        Author(
            id = id,
            name = name,
            identifiers = identifiers,
            homepage = homepage
        )
}
