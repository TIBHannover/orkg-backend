package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.net.URI
import java.time.OffsetDateTime
import org.eclipse.rdf4j.model.Model

data class Class(
    val id: ClassId? = null,
    override val label: String = "Label",
    val uri: URI? = null,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime? = OffsetDateTime.now(),
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "class"
) : Thing {
    @JsonIgnore
    var rdf: Model? = null

    @JsonProperty("description")
    var description: String? = null
    fun toClass(): Class = Class(id, label, uri, createdAt, createdBy, _class)
    override val thingId: String?
        get() = id?.toString()
}
