package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.rdf4j.model.Model
import java.net.URI
import java.time.OffsetDateTime

data class Class(
    val id: ClassId?,
    val label: String,
    val uri: URI?,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?
) {
    @JsonIgnore
    var rdf: Model? = null
}
