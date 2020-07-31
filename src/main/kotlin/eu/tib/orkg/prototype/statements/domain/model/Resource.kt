package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import java.time.OffsetDateTime
import java.util.UUID
import org.eclipse.rdf4j.model.Model

data class Resource(
    val id: ResourceId?,
    override val label: String,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    val classes: Set<ClassId> = emptySet(),
    val shared: Int = 0,
    @JsonProperty("created_by")
    val createdBy: UUID = UUID(0, 0),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "resource",
    @JsonProperty("observatory_id")
    val observatoryId: UUID = UUID(0, 0),
    @JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    @JsonProperty("organization_id")
    val organizationId: UUID = UUID(0, 0)
) : Thing {
    @JsonIgnore
    var rdf: Model? = null

    @JsonProperty("formatted_label")
    var formattedLabel: String? = null

    val hasClasses: Boolean
        get() = this.classes.isNotEmpty()
}
