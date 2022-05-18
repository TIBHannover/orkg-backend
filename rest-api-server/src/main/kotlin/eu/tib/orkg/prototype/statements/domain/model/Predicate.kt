package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import java.time.OffsetDateTime
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS

data class Predicate(
    val id: PredicateId?,
    val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "predicate"
) : Thing {
    @JsonIgnore
    var rdf: Model? = null

    @JsonProperty("description")
    var description: String? = null
}

fun Predicate.toNTriple(): String {
    val cPrefix = RdfConstants.CLASS_NS
    val pPrefix = RdfConstants.PREDICATE_NS
    return "<$pPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Predicate> .\n" +
        "<$pPrefix$id> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n"
}

fun Predicate.toRdfModel(): Model {
    val builder = ModelBuilder()
        .setNamespace("p", RdfConstants.PREDICATE_NS)
        .setNamespace("c", RdfConstants.CLASS_NS)
        .setNamespace(RDF.NS)
        .setNamespace(RDFS.NS)
        .subject("p:$id")
        .add(RDFS.LABEL, label)
        .add(RDF.TYPE, "c:Predicate")
    return builder.build()
}
