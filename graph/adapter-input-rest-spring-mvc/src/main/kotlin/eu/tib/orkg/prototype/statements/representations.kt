package eu.tib.orkg.prototype.statements

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
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

interface LiteralRepresentation : ThingRepresentation, ProvenanceMetadata {
    val label: String
    val datatype: String

    // This is added to replace @JsonTypeInfo on the Thing interface
    @get:JsonProperty("_class")
    val jsonClass: String
}

interface ClassRepresentation : ThingRepresentation, ProvenanceMetadata {
    val label: String
    val uri: URI?
    val description: String?

    // This is added to replace @JsonTypeInfo on the Thing interface
    @get:JsonProperty("_class")
    val jsonClass: String
}

interface PredicateRepresentation : ThingRepresentation, ProvenanceMetadata {
    val label: String
    val description: String?

    // This is added to replace @JsonTypeInfo on the Thing interface
    @get:JsonProperty("_class")
    val jsonClass: String
}

interface ResourceRepresentation : ThingRepresentation, ResourceProvenanceMetadata, ContentTypeFlags {
    val label: String
    val classes: Set<ThingId>
    val shared: Long
    @get:JsonProperty("formatted_label")
    val formattedLabel: FormattedLabel?

    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod

    // This is added to replace @JsonTypeInfo on the Thing interface
    @get:JsonProperty("_class")
    val jsonClass: String
}

interface ResourceProvenanceMetadata : ProvenanceMetadata {
    @get:JsonProperty("observatory_id")
    val observatoryId: ObservatoryId

    @get:JsonProperty("organization_id")
    val organizationId: OrganizationId
}

interface ProvenanceMetadata {
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime

    @get:JsonProperty("created_by")
    val createdBy: ContributorId
}

interface ContentTypeFlags {
    // TODO: split into separate representations, content types only
    val featured: Boolean
    val unlisted: Boolean
    val verified: Boolean
}

interface PaperResourceWithPathRepresentation : ResourceRepresentation {
    val path: PathRepresentation
}

data class BundleRepresentation(
    @JsonProperty("root")
    val rootId: ThingId,
    @JsonProperty("statements")
    val bundle: List<StatementRepresentation>
)

sealed interface AuthorRepresentation {
    interface ResourceAuthorRepresentation : AuthorRepresentation {
        val value: ResourceRepresentation
    }
    interface LiteralAuthorRepresentation : AuthorRepresentation {
        val value: String
    }
}

interface PaperAuthorRepresentation {
    val author: AuthorRepresentation
    val papers: Int
}

interface ComparisonAuthorRepresentation {
    val author: AuthorRepresentation
    val info: Iterable<ComparisonAuthorInfo>
}

interface PaperCountPerResearchProblemRepresentation {
    val problem: ResourceRepresentation
    val papers: Long
}

interface FieldWithFreqRepresentation {
    val field: ResourceRepresentation
    val freq: Long
}

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


interface StatementRepresentation : StatementProvenanceMetadata {
    val id: StatementId
    val subject: ThingRepresentation
    val predicate: PredicateRepresentation
    val `object`: ThingRepresentation
}

interface StatementProvenanceMetadata {
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime

    @get:JsonProperty("created_by")
    val createdBy: ContributorId
}
