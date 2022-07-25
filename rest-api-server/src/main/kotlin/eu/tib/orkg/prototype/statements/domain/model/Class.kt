package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

data class Class(
    val id: ClassId?,
    val label: String,
    val uri: URI?,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "class"
) : Thing {
    var description: String? = null
    fun toClass(): Class = Class(id, label, uri, createdAt, createdBy, _class)
}

internal fun Class?.toOptional() = Optional.ofNullable(this)

fun Class.toNTriple(): String {
    val cPrefix = RdfConstants.CLASS_NS
    val sb = StringBuilder()
    sb.append("<$cPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .\n")
    if (uri != null)
        sb.append("<$cPrefix$id> <http://www.w3.org/2002/07/owl#equivalentClass> <$uri> .\n")
    sb.append("<$cPrefix$id> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
    return sb.toString()
}
