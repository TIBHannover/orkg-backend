package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime
import org.eclipse.rdf4j.model.Model

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
