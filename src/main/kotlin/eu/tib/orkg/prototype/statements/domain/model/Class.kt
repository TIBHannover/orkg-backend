package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.net.URI
import java.time.OffsetDateTime
import org.eclipse.rdf4j.model.Model

data class Class(
    val id: ClassId?,
    val label: String,
    val uri: URI?,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "class"
) : Thing {
    @JsonIgnore
    var rdf: Model? = null

    fun toClass(): Class = Class(id, label, uri, createdAt, createdBy, _class)
}
