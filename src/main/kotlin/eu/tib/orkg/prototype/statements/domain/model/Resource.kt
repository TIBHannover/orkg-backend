package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import java.time.OffsetDateTime
import org.eclipse.rdf4j.model.Model

data class Resource(
    val id: ResourceId?,
    val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    val classes: Set<ClassId> = emptySet(),
    val shared: Int = 0,
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "resource",
    @JsonProperty("observatory_id")
    val observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    @JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    @JsonProperty("organization_id")
    val organizationId: OrganizationId = OrganizationId.createUnknownOrganization()
) : Thing {
    @JsonIgnore
    var rdf: Model? = null
}
