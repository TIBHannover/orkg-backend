package eu.tib.orkg.prototype.statements.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.*
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthorInfo
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI
import java.time.OffsetDateTime

typealias PathRepresentation = List<List<ThingRepresentation>>

sealed interface ThingRepresentation {
    val id: ThingId
}

interface ProvenanceMetadata {
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime

    @get:JsonProperty("created_by")
    val createdBy: ContributorId
}

interface ResourceProvenanceMetadata : ProvenanceMetadata {
    @get:JsonProperty("observatory_id")
    val observatoryId: ObservatoryId

    @get:JsonProperty("organization_id")
    val organizationId: OrganizationId
}

interface ContentTypeFlags {
    // TODO: split into separate representations, content types only
    val featured: Boolean
    val unlisted: Boolean
    val verified: Boolean
    @get:JsonInclude(Include.NON_NULL)
    @get:JsonProperty("unlisted_by")
    val unlistedBy: ContributorId?
}

data class LiteralRepresentation(
    override val id: ThingId,
    val label: String,
    val datatype: String,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    // This is added to replace @JsonTypeInfo on the Thing data class
    @get:JsonProperty("_class")
    val jsonClass: String = "literal"
) : ThingRepresentation, ProvenanceMetadata

data class ClassRepresentation(
    override val id: ThingId,
    val label: String,
    val uri: URI?,
    val description: String?,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    // This is added to replace @JsonTypeInfo on the Thing data class
    @get:JsonProperty("_class")
    val jsonClass: String = "class"
) : ThingRepresentation, ProvenanceMetadata

data class PredicateRepresentation(
    override val id: ThingId,
    val label: String,
    val description: String?,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    // This is added to replace @JsonTypeInfo on the Thing data class
    @get:JsonProperty("_class")
    val jsonClass: String = "predicate"
) : ThingRepresentation, ProvenanceMetadata

data class ResourceRepresentation(
    override val id: ThingId,
    val label: String,
    val classes: Set<ThingId>,
    val shared: Long,
    override val observatoryId: ObservatoryId,
    override val organizationId: OrganizationId,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    override val featured: Boolean,
    override val unlisted: Boolean,
    override val verified: Boolean,
    override val unlistedBy: ContributorId?,
    @get:JsonProperty("formatted_label")
    val formattedLabel: FormattedLabel?,
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    // This is added to replace @JsonTypeInfo on the Thing data class
    @get:JsonProperty("_class")
    val jsonClass: String = "resource"
) : ThingRepresentation, ResourceProvenanceMetadata, ContentTypeFlags

data class StatementRepresentation(
    val id: StatementId,
    val subject: ThingRepresentation,
    val predicate: PredicateRepresentation,
    val `object`: ThingRepresentation,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    @get:JsonInclude(Include.NON_NULL)
    val index: Int?
) : ProvenanceMetadata

data class ListRepresentation(
    val id: ThingId,
    val label: String,
    val elements: List<ThingId>,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    @get:JsonProperty("_class")
    val jsonClass: String = "list"
) : ProvenanceMetadata

// TODO: Replace with data class that has two fields:
//  resource: ResourceRepresentation
//  path: PathRepresentation
data class PaperResourceWithPathRepresentation(
    val path: PathRepresentation,
    override val id: ThingId,
    val label: String,
    val classes: Set<ThingId>,
    val shared: Long,
    override val observatoryId: ObservatoryId,
    override val organizationId: OrganizationId,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId,
    override val featured: Boolean,
    override val unlisted: Boolean,
    override val verified: Boolean,
    override val unlistedBy: ContributorId?,
    @get:JsonProperty("formatted_label")
    val formattedLabel: FormattedLabel?,
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    // This is added to replace @JsonTypeInfo on the Thing data class
    @get:JsonProperty("_class")
    val jsonClass: String = "resource"
) : ThingRepresentation, ResourceProvenanceMetadata, ContentTypeFlags

data class BundleRepresentation(
    @JsonProperty("root")
    val rootId: ThingId,
    @JsonProperty("statements")
    val bundle: List<StatementRepresentation>
)

sealed interface AuthorRepresentation {
    data class ResourceAuthorRepresentation(
        val value: ResourceRepresentation
    ) : AuthorRepresentation

    data class LiteralAuthorRepresentation(
        val value: String
    ) : AuthorRepresentation
}

data class PaperAuthorRepresentation(
    val author: AuthorRepresentation,
    val papers: Int
)

data class ComparisonAuthorRepresentation(
    val author: AuthorRepresentation,
    val info: Iterable<ComparisonAuthorInfo>
)

data class PaperCountPerResearchProblemRepresentation(
    val problem: ResourceRepresentation,
    val papers: Long
)

data class FieldWithFreqRepresentation(
    val field: ResourceRepresentation,
    val freq: Long
)

data class ChildClassRepresentation(
    val `class`: ClassRepresentation,
    @JsonProperty("child_count")
    val childCount: Long
)

data class ClassHierarchyEntryRepresentation(
    val `class`: ClassRepresentation,
    @JsonProperty("parent_id")
    val parentId: ThingId?
)

data class ResearchFieldWithChildCountRepresentation(
    val resource: ResourceRepresentation,
    @JsonProperty("child_count")
    val childCount: Long
)

data class ResearchFieldHierarchyEntryRepresentation(
    val resource: ResourceRepresentation,
    @JsonProperty("parent_ids")
    val parentIds: Set<ThingId>
)
