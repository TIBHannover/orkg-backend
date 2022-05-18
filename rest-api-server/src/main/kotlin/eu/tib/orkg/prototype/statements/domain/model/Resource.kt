package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import java.lang.StringBuilder
import org.eclipse.rdf4j.model.Model
import java.time.OffsetDateTime

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
    val organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    @JsonProperty("featured")
    val featured: Boolean? = null,
    @JsonProperty("unlisted")
    val unlisted: Boolean? = null,
    val verified: Boolean? = null,
) : Thing {
    @JsonIgnore
    var rdf: Model? = null
}

fun Resource.toNTriple(): String {
    val cPrefix = RdfConstants.CLASS_NS
    val rPrefix = RdfConstants.RESOURCE_NS
    val sb = StringBuilder()
    sb.append("<$rPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Resource> .\n")
    classes.forEach { sb.append("<$rPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <$cPrefix${it.value}> .\n") }
    sb.append("<$rPrefix$id> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
    return sb.toString()
}
