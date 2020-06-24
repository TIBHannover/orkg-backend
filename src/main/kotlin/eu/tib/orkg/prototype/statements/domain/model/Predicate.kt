package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID
import org.eclipse.rdf4j.model.Model

data class Predicate(
    val id: PredicateId?,
    override val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: UUID = UUID(0, 0),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "predicate"
) : Thing {
    @JsonIgnore
    var rdf: Model? = null
}
