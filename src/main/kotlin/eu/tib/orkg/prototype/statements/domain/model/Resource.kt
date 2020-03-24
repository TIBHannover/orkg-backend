package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID
import org.eclipse.rdf4j.model.Model

data class Resource(
    val id: ResourceId?,
    val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    val classes: Set<ClassId> = emptySet(),
    val shared: Int = 0,
    @JsonProperty("created_by")
    val createdBy: UUID = UUID(0, 0),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "resource"
) : Thing {
    @JsonIgnore
    var rdf: Model? = null
}
