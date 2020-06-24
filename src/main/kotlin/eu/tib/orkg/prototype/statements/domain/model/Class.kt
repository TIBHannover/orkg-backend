package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID
import org.eclipse.rdf4j.model.Model

data class Class(
    val id: ClassId?,
    override val label: String,
    val uri: URI?,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: UUID = UUID(0, 0),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "class"
) : Thing {
    @JsonIgnore
    var rdf: Model? = null
}
