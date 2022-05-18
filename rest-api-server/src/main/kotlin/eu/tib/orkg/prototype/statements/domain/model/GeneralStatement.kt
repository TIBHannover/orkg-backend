package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.application.StatementResponse
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import java.time.OffsetDateTime

data class GeneralStatement(
    val id: StatementId? = null,
    val subject: Thing,
    val predicate: Predicate,
    val `object`: Thing,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.createUnknownContributor()

) : StatementResponse

data class CreateStatement(
    val id: StatementId? = null,
    @JsonProperty("subject_id")
    val subjectId: String,
    @JsonProperty("predicate_id")
    val predicateId: PredicateId,
    @JsonProperty("object_id")
    val objectId: String
)
/**
 * Convert the triple to a statement in NTriple format.
 */
fun GeneralStatement.toNTriple(): String {
    val pPrefix = RdfConstants.PREDICATE_NS
    val result = "${serializeThing(subject)} <$pPrefix${predicate.id}> ${serializeThing(`object`)} .\n"
    if (result[0] == '"')
    // Ignore literal
    // TODO: log this somewhere
        return ""
    return result
}

private fun serializeThing(thing: Thing): String {
    val rPrefix = RdfConstants.RESOURCE_NS
    val pPrefix = RdfConstants.PREDICATE_NS
    val cPrefix = RdfConstants.CLASS_NS
    return when (thing) {
        is Resource -> "<$rPrefix${thing.id}>"
        is Predicate -> "<$pPrefix${thing.id}>"
        is Class -> "<$cPrefix${thing.id}>"
        is Literal -> "\"${escapeLiterals(thing.label)}\"^^<http://www.w3.org/2001/XMLSchema#string>"
        else -> throw IllegalStateException("Unable to convert unknown Thing to RDF: $thing")
    }
}
