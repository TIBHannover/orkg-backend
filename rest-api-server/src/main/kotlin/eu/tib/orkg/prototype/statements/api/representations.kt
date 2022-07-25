package eu.tib.orkg.prototype.statements.api

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.net.URI
import java.time.OffsetDateTime

sealed interface ThingRepresentation

interface LiteralRepresentation : ThingRepresentation, ProvenanceMetadata {
    val id: LiteralId
    val label: String
    val datatype: String

    // This is added to replace @JsonTypeInfo on the Thing interface
    @get:JsonProperty("_class")
    val jsonClass: String
}

interface ClassRepresentation : ThingRepresentation, ProvenanceMetadata {
    val id: ClassId
    val label: String
    val uri: URI?
    val description: String?

    // This is added to replace @JsonTypeInfo on the Thing interface
    @get:JsonProperty("_class")
    val jsonClass: String
}

interface PredicateRepresentation : ThingRepresentation, ProvenanceMetadata {
    val id: PredicateId
    val label: String
    val description: String?

    // This is added to replace @JsonTypeInfo on the Thing interface
    @get:JsonProperty("_class")
    val jsonClass: String
}

interface ResourceRepresentation : ThingRepresentation, ResourceProvenanceMetadata, ContentTypeFlags {
    val id: ResourceId
    val label: String
    val classes: Set<ClassId>
    val shared: Long

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
