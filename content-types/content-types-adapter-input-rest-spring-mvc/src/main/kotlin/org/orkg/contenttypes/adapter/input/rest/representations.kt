package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.time.OffsetDateTime
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PredicateReference
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.domain.ThingReference
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.ThingRepresentation
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Visibility

data class PaperRepresentation(
    val id: ThingId,
    val title: String,
    @get:JsonProperty("research_fields")
    val researchFields: List<LabeledObjectRepresentation>,
    val identifiers: Map<String, List<String>>,
    @get:JsonProperty("publication_info")
    val publicationInfo: PublicationInfoRepresentation,
    val authors: List<AuthorRepresentation>,
    val contributions: List<LabeledObjectRepresentation>,
    @get:JsonProperty("sdgs")
    val sustainableDevelopmentGoals: Set<LabeledObjectRepresentation>,
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
    val modifiable: Boolean,
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
    val publishedIn: ObjectIdAndLabel?,
    val url: URI?
)

data class AuthorRepresentation(
    val id: ThingId?,
    val name: String,
    val identifiers: Map<String, List<String>>,
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
    val identifiers: Map<String, List<String>>,
    @get:JsonProperty("publication_info")
    val publicationInfo: PublicationInfoRepresentation,
    val authors: List<AuthorRepresentation>,
    @get:JsonProperty("sdgs")
    val sustainableDevelopmentGoals: Set<LabeledObjectRepresentation>,
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
    @get:JsonProperty("versions")
    val versions: List<HeadVersionRepresentation>,
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

data class VersionInfoRepresentation(
    val head: HeadVersionRepresentation,
    val published: List<PublishedVersionRepresentation>
)

data class HeadVersionRepresentation(
    val id: ThingId,
    val label: String,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime
)

data class PublishedVersionRepresentation(
    val id: ThingId,
    val label: String,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    val changelog: String?
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
    @field:Valid
    val identifiers: Map<String, List<String>>?,
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
    fun toPublicationInfoDefinition(): PublicationInfoDefinition =
        PublicationInfoDefinition(
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
    val targetClass: ClassReference,
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
    val placeholder: String?
    val description: String?
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
    override val placeholder: String?,
    override val description: String?,
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
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val pattern: String?,
    override val path: ObjectIdAndLabel,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    val `class`: ObjectIdAndLabel
) : TemplatePropertyRepresentation

data class RosettaTemplateRepresentation(
    val id: ThingId,
    val label: String,
    val description: String?,
    @get:JsonProperty("formatted_label")
    val formattedLabel: FormattedLabel?,
    @get:JsonProperty("target_class")
    val targetClass: ThingId,
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

data class TemplateInstanceRepresentation(
    val root: ResourceRepresentation,
    val statements: Map<ThingId, List<EmbeddedStatementRepresentation>>
)

data class EmbeddedStatementRepresentation(
    val thing: ThingRepresentation,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    val statements: Map<ThingId, List<EmbeddedStatementRepresentation>>
)

data class LiteratureListRepresentation(
    val id: ThingId,
    val title: String,
    @get:JsonProperty("research_fields")
    val researchFields: List<ObjectIdAndLabel>,
    val authors: List<AuthorRepresentation>,
    val versions: VersionInfoRepresentation,
    @get:JsonProperty("sdgs")
    val sustainableDevelopmentGoals: Set<LabeledObjectRepresentation>,
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
    val unlistedBy: ContributorId? = null,
    val published: Boolean,
    val sections: List<LiteratureListSectionRepresentation>
)

sealed interface LiteratureListSectionRepresentation {
    val id: ThingId
    val type: String
}

data class ListSectionRepresentation(
    override val id: ThingId,
    val entries: List<ResourceReference>,
    override val type: String = "list"
) : LiteratureListSectionRepresentation

data class TextSectionRepresentation(
    override val id: ThingId,
    val heading: String,
    @get:JsonProperty("heading_size")
    val headingSize: Int,
    val text: String,
    override val type: String = "text"
) : LiteratureListSectionRepresentation

data class SmartReviewRepresentation(
    val id: ThingId,
    val title: String,
    @get:JsonProperty("research_fields")
    val researchFields: List<ObjectIdAndLabel>,
    val authors: List<AuthorRepresentation>,
    val versions: VersionInfoRepresentation,
    @get:JsonProperty("sdgs")
    val sustainableDevelopmentGoals: Set<LabeledObjectRepresentation>,
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
    val unlistedBy: ContributorId? = null,
    val published: Boolean,
    val sections: List<SmartReviewSectionRepresentation>,
    val references: List<String>
)

sealed interface SmartReviewSectionRepresentation {
    val id: ThingId
    val heading: String
    val type: String
}

data class SmartReviewComparisonSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val comparison: ResourceReference?,
    override val type: String = "comparison"
) : SmartReviewSectionRepresentation

data class SmartReviewVisualizationSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val visualization: ResourceReference?,
    override val type: String = "visualization"
) : SmartReviewSectionRepresentation

data class SmartReviewResourceSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val resource: ResourceReference?,
    override val type: String = "resource"
) : SmartReviewSectionRepresentation

data class SmartReviewPredicateSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val predicate: PredicateReference?,
    override val type: String = "property"
) : SmartReviewSectionRepresentation

data class SmartReviewOntologySectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val entities: List<ThingReference>,
    val predicates: List<PredicateReference>,
    override val type: String = "ontology"
) : SmartReviewSectionRepresentation

data class SmartReviewTextSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val classes: Set<ThingId>,
    val text: String,
    override val type: String = "text"
) : SmartReviewSectionRepresentation
