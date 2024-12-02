package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.OffsetDateTime
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.LiteralReference
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PredicateReference
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.ThingRepresentation
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

sealed interface ContentTypeRepresentation {
    @get:JsonProperty("_class")
    val jsonClass: String
}

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
    val mentionings: Set<ResourceReferenceRepresentation>,
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
    val unlistedBy: ContributorId?,
    override val jsonClass: String = "paper"
) : ContentTypeRepresentation

data class PublicationInfoRepresentation(
    @get:JsonProperty("published_month")
    val publishedMonth: Int?,
    @get:JsonProperty("published_year")
    val publishedYear: Long?,
    @get:JsonProperty("published_in")
    val publishedIn: ObjectIdAndLabel?,
    val url: ParsedIRI?
)

data class AuthorRepresentation(
    val id: ThingId?,
    val name: String,
    val identifiers: Map<String, List<String>>,
    val homepage: ParsedIRI?
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
    val unlistedBy: ContributorId?,
    override val jsonClass: String = "comparison"
) : ContentTypeRepresentation

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
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
)

data class PublishedVersionRepresentation(
    val id: ThingId,
    val label: String,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
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
    val unlistedBy: ContributorId?,
    override val jsonClass: String = "visualization"
) : ContentTypeRepresentation

data class AuthorDTO(
    val id: ThingId?,
    @field:NotBlank
    val name: String,
    @field:Valid
    val identifiers: IdentifierMapDTO?,
    val homepage: ParsedIRI?
) {
    fun toAuthor(): Author =
        Author(
            id = id,
            name = name,
            identifiers = identifiers?.values,
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
    val url: ParsedIRI?
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
    val formattedLabel: String?, // FIXME: The type should be FormattedLabel, but value classes cannot be parsed by jackson
    @get:JsonProperty("target_class")
    val targetClass: ClassReferenceRepresentation,
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
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    val visibility: Visibility,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?,
    override val jsonClass: String = "template"
) : ContentTypeRepresentation

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
    val path: ObjectIdAndLabel
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime
    @get:JsonProperty("created_by")
    val createdBy: ContributorId
}

data class UntypedTemplatePropertyRepresentation(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ObjectIdAndLabel,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId
) : TemplatePropertyRepresentation

sealed interface LiteralTemplatePropertyRepresentation : TemplatePropertyRepresentation {
    val datatype: ClassReferenceRepresentation
}

data class StringLiteralTemplatePropertyRepresentation(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    val pattern: String?,
    override val path: ObjectIdAndLabel,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    override val datatype: ClassReferenceRepresentation,
) : LiteralTemplatePropertyRepresentation

data class NumberLiteralTemplatePropertyRepresentation(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    @JsonProperty("min_inclusive")
    val minInclusive: RealNumber?,
    @JsonProperty("max_inclusive")
    val maxInclusive: RealNumber?,
    override val path: ObjectIdAndLabel,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    override val datatype: ClassReferenceRepresentation
) : LiteralTemplatePropertyRepresentation

data class OtherLiteralTemplatePropertyRepresentation(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ObjectIdAndLabel,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    override val datatype: ClassReferenceRepresentation
) : LiteralTemplatePropertyRepresentation

data class ResourceTemplatePropertyRepresentation(
    override val id: ThingId,
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    override val order: Long,
    override val minCount: Int?,
    override val maxCount: Int?,
    override val path: ObjectIdAndLabel,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    val `class`: ObjectIdAndLabel
) : TemplatePropertyRepresentation

data class RosettaStoneTemplateRepresentation(
    val id: ThingId,
    val label: String,
    val description: String?,
    @get:JsonProperty("formatted_label")
    val formattedLabel: String?, // FIXME: The type should be FormattedLabel, but value classes cannot be parsed by jackson
    @get:JsonProperty("target_class")
    val targetClass: ThingId,
    @get:JsonProperty("example_usage")
    val exampleUsage: String?,
    val properties: List<TemplatePropertyRepresentation>,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val visibility: Visibility,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?,
    val modifiable: Boolean
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
    val sections: List<LiteratureListSectionRepresentation>,
    val acknowledgements: Map<ContributorId, Double>,
    override val jsonClass: String = "literature-list"
) : ContentTypeRepresentation

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(ListSectionRepresentation::class),
    JsonSubTypes.Type(TextSectionRepresentation::class)
])
sealed interface LiteratureListSectionRepresentation {
    val id: ThingId
}

@JsonTypeName("list")
data class ListSectionRepresentation(
    override val id: ThingId,
    val entries: List<EntryRepresentation>
) : LiteratureListSectionRepresentation {
    data class EntryRepresentation(
        val value: ResourceReferenceRepresentation,
        val description: String?
    )
}

@JsonTypeName("text")
data class TextSectionRepresentation(
    override val id: ThingId,
    val heading: String,
    @get:JsonProperty("heading_size")
    val headingSize: Int,
    val text: String
) : LiteratureListSectionRepresentation

data class SmartReviewRepresentation(
    val id: ThingId,
    val title: String,
    @get:JsonProperty("research_fields")
    val researchFields: List<ObjectIdAndLabel>,
    val identifiers: Map<String, List<String>>,
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
    val references: List<String>,
    val acknowledgements: Map<ContributorId, Double>,
    override val jsonClass: String = "smart-review"
) : ContentTypeRepresentation

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(SmartReviewComparisonSectionRepresentation::class),
    JsonSubTypes.Type(SmartReviewVisualizationSectionRepresentation::class),
    JsonSubTypes.Type(SmartReviewResourceSectionRepresentation::class),
    JsonSubTypes.Type(SmartReviewPredicateSectionRepresentation::class),
    JsonSubTypes.Type(SmartReviewOntologySectionRepresentation::class),
    JsonSubTypes.Type(SmartReviewTextSectionRepresentation::class)
])
sealed interface SmartReviewSectionRepresentation {
    val id: ThingId
    val heading: String
}

@JsonTypeName("comparison")
data class SmartReviewComparisonSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val comparison: ResourceReferenceRepresentation?
) : SmartReviewSectionRepresentation

@JsonTypeName("visualization")
data class SmartReviewVisualizationSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val visualization: ResourceReferenceRepresentation?
) : SmartReviewSectionRepresentation

@JsonTypeName("resource")
data class SmartReviewResourceSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val resource: ResourceReferenceRepresentation?
) : SmartReviewSectionRepresentation

@JsonTypeName("property")
data class SmartReviewPredicateSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val predicate: PredicateReferenceRepresentation?
) : SmartReviewSectionRepresentation

@JsonTypeName("ontology")
data class SmartReviewOntologySectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val entities: List<ThingReferenceRepresentation>,
    val predicates: List<PredicateReferenceRepresentation>
) : SmartReviewSectionRepresentation

@JsonTypeName("text")
data class SmartReviewTextSectionRepresentation(
    override val id: ThingId,
    override val heading: String,
    val classes: Set<ThingId>,
    val text: String
) : SmartReviewSectionRepresentation

data class RosettaStoneStatementRepresentation(
    val id: ThingId,
    @get:JsonProperty("context")
    val context: ThingId?,
    @get:JsonProperty("template_id")
    val templateId: ThingId,
    @get:JsonProperty("class_id")
    val classId: ThingId,
    @get:JsonProperty("version_id")
    val versionId: ThingId,
    @get:JsonProperty("latest_version_id")
    val latestVersion: ThingId,
    @get:JsonProperty("formatted_label")
    val formattedLabel: String,
    val subjects: List<ThingReferenceRepresentation>,
    val objects: List<List<ThingReferenceRepresentation>>,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    val certainty: Certainty,
    val negated: Boolean,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    val visibility: Visibility,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?,
    val modifiable: Boolean,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("deleted_by")
    val deletedBy: ContributorId?,
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("deleted_at")
    val deletedAt: OffsetDateTime?
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "_class"
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(PredicateReference::class),
    JsonSubTypes.Type(ClassReference::class),
    JsonSubTypes.Type(LiteralReference::class),
    JsonSubTypes.Type(ResourceReference::class),
])
sealed interface ThingReferenceRepresentation {
    val id: ThingId?
    val label: String
}

@JsonTypeName("resource_ref")
data class ResourceReferenceRepresentation(
    override val id: ThingId,
    override val label: String,
    val classes: Set<ThingId>
) : ThingReferenceRepresentation

@JsonTypeName("predicate_ref")
data class PredicateReferenceRepresentation(
    override val id: ThingId,
    override val label: String
) : ThingReferenceRepresentation

@JsonTypeName("class_ref")
data class ClassReferenceRepresentation(
    override val id: ThingId,
    override val label: String,
    val uri: ParsedIRI?
) : ThingReferenceRepresentation

@JsonTypeName("literal_ref")
@JsonIgnoreProperties(ignoreUnknown = true)
data class LiteralReferenceRepresentation(
    override val label: String,
    val datatype: String
) : ThingReferenceRepresentation {
    override val id: ThingId? get() = null
}

data class TableRepresentation(
    val id: ThingId,
    val label: String,
    val rows: List<RowRepresentation>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    val visibility: Visibility,
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId? = null
) {
    data class RowRepresentation(
        val label: String?,
        val data: List<ThingReferenceRepresentation?>
    )
}
