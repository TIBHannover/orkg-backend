package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import java.time.OffsetDateTime

data class Resource(
    val id: ResourceId?,
    val label: String,
    val createdAt: OffsetDateTime,
    val classes: Set<ClassId> = emptySet(),
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "resource",
    val observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    val organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    val featured: Boolean? = null,
    val unlisted: Boolean? = null,
    val verified: Boolean? = null,
) : Thing

fun Resource.toNTriple(): String {
    val cPrefix = RdfConstants.CLASS_NS
    val rPrefix = RdfConstants.RESOURCE_NS
    val sb = StringBuilder()
    sb.append("<$rPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Resource> .\n")
    classes.forEach { sb.append("<$rPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <$cPrefix${it.value}> .\n") }
    sb.append("<$rPrefix$id> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
    return sb.toString()
}
