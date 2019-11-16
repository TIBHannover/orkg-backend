package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.rdf4j.model.Model
import java.time.OffsetDateTime

data class Predicate(
    val id: PredicateId?,
    val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?
) {
    @JsonIgnore
    var rdf: Model? = null
}
