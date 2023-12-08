package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.time.OffsetDateTime
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
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
    val visibility: Visibility,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?
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
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    val visibility: Visibility,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?
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
    val visibility: Visibility,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?
)

data class ComparisonRelatedResourceRepresentation(
    val id: ThingId,
    val label: String,
    val image: String?,
    val url: String?,
    val description: String?,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
)

data class ComparisonRelatedFigureRepresentation(
    val id: ThingId,
    val label: String,
    val image: String?,
    val description: String?,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
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
    val visibility: Visibility,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?
)

data class AuthorDTO(
    val id: ThingId?,
    @field:NotBlank
    val name: String,
    val identifiers: Map<String, String>?,
    val homepage: URI?
) {
    fun toAuthor(): Author =
        Author(
            id = id,
            name = name,
            identifiers = identifiers,
            homepage = homepage
        )
}

data class PublicationInfoDTO(
    @field:Min(1)
    @field:Max(12)
    @JsonProperty("published_month")
    val publishedMonth: Int?,
    @JsonProperty("published_year")
    val publishedYear: Long?,
    @field:Size(min = 1)
    @JsonProperty("published_in")
    val publishedIn: String?,
    val url: URI?
) {
    fun toPublicationInfo(): PublicationInfo =
        PublicationInfo(
            publishedMonth = publishedMonth,
            publishedYear = publishedYear,
            publishedIn = publishedIn,
            url = url
        )
}

data class TemplateRepresentation(
    val id: ThingId,
    val label: String,
    val description: String?,
    @get:JsonProperty("formatted_label")
    val formattedLabel: FormattedLabel?,
    @get:JsonProperty("target_class")
    val targetClass: ThingId,
    val relations: TemplateRelationRepresentation,
    val properties: List<TemplatePropertyRepresentation>,
    @get:JsonProperty("is_closed")
    val isClosed: Boolean,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val visibility: Visibility,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?
)

data class TemplateRelationRepresentation(
    @get:JsonProperty("research_fields")
    val researchFields: List<ObjectIdAndLabel>,
    @get:JsonProperty("research_problems")
    val researchProblems: List<ObjectIdAndLabel>,
    val predicate: ObjectIdAndLabel?,
)

sealed interface TemplatePropertyRepresentation {
    val id: ThingId
    val label: String
    val order: Long
    @get:JsonProperty("min_count")
    val minCount: Int?
    @get:JsonProperty("max_count")
    val maxCount: Int?
    val pattern: String?
    val path: ObjectIdAndLabel
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
}

data class LiteralTemplatePropertyRepresentation(
    override val id: ThingId,
    override val label: String,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ObjectIdAndLabel,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    val datatype: ObjectIdAndLabel
) : TemplatePropertyRepresentation

data class ResourceTemplatePropertyRepresentation(
    override val id: ThingId,
    override val label: String,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ObjectIdAndLabel,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    val `class`: ObjectIdAndLabel
) : TemplatePropertyRepresentation
