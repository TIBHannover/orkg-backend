package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.input.CreateClassCommandPart
import org.orkg.contenttypes.input.CreateListCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.CreateRowCommand
import org.orkg.contenttypes.input.NumberLiteralPropertyCommand
import org.orkg.contenttypes.input.OtherLiteralPropertyCommand
import org.orkg.contenttypes.input.ResourcePropertyCommand
import org.orkg.contenttypes.input.StringLiteralPropertyCommand
import org.orkg.contenttypes.input.TemplatePropertyCommand
import org.orkg.contenttypes.input.UntypedPropertyCommand
import org.orkg.contenttypes.input.UpdateRowCommand
import org.orkg.graph.domain.Literals

data class CreateListRequestPart(
    @field:NotBlank
    val label: String,
    val elements: List<String>,
) {
    fun toCreateCommand(): CreateListCommandPart =
        CreateListCommandPart(
            label = label,
            elements = elements
        )
}

data class CreateLiteralRequestPart(
    @field:NotBlank
    val label: String,
    @JsonProperty("data_type")
    val dataType: String?,
) {
    fun toCreateCommand(): CreateLiteralCommandPart =
        CreateLiteralCommandPart(
            label = label,
            dataType = dataType ?: Literals.XSD.STRING.prefixedUri
        )
}

data class CreatePredicateRequestPart(
    @field:NotBlank
    val label: String,
    @field:NotBlank
    val description: String?,
) {
    fun toCreateCommand(): CreatePredicateCommandPart =
        CreatePredicateCommandPart(
            label = label,
            description = description
        )
}

data class CreateResourceRequestPart(
    @field:NotBlank
    val label: String,
    val classes: Set<ThingId>?,
) {
    fun toCreateCommand(): CreateResourceCommandPart =
        CreateResourceCommandPart(
            label = label,
            classes = classes.orEmpty()
        )
}

data class CreateClassRequestPart(
    @field:NotBlank
    val label: String,
    val uri: ParsedIRI? = null,
) {
    fun toCreateCommand(): CreateClassCommandPart =
        CreateClassCommandPart(
            label = label,
            uri = uri
        )
}

sealed interface TemplatePropertyRequest {
    val label: String
    val placeholder: String?
    val description: String?
    val minCount: Int?
    val maxCount: Int?
    val path: ThingId

    fun toTemplatePropertyCommand(): TemplatePropertyCommand
}

data class UntypedPropertyRequest(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    @field:PositiveOrZero
    @JsonProperty("min_count")
    override val minCount: Int?,
    @field:PositiveOrZero
    @JsonProperty("max_count")
    override val maxCount: Int?,
    override val path: ThingId,
) : TemplatePropertyRequest {
    override fun toTemplatePropertyCommand(): TemplatePropertyCommand =
        UntypedPropertyCommand(label, placeholder, description, minCount, maxCount, path)
}

data class StringLiteralPropertyRequest(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    @field:PositiveOrZero
    @JsonProperty("min_count")
    override val minCount: Int?,
    @field:PositiveOrZero
    @JsonProperty("max_count")
    override val maxCount: Int?,
    val pattern: String?,
    override val path: ThingId,
    val datatype: ThingId,
) : TemplatePropertyRequest {
    override fun toTemplatePropertyCommand(): TemplatePropertyCommand =
        StringLiteralPropertyCommand(label, placeholder, description, minCount, maxCount, pattern, path, datatype)
}

data class NumberLiteralPropertyRequest(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    @field:PositiveOrZero
    @JsonProperty("min_count")
    override val minCount: Int?,
    @field:PositiveOrZero
    @JsonProperty("max_count")
    override val maxCount: Int?,
    @JsonProperty("min_inclusive")
    val minInclusive: RealNumber?,
    @JsonProperty("max_inclusive")
    val maxInclusive: RealNumber?,
    override val path: ThingId,
    val datatype: ThingId,
) : TemplatePropertyRequest {
    override fun toTemplatePropertyCommand(): TemplatePropertyCommand =
        NumberLiteralPropertyCommand(label, placeholder, description, minCount, maxCount, minInclusive, maxInclusive, path, datatype)
}

data class OtherLiteralPropertyRequest(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    @field:PositiveOrZero
    @JsonProperty("min_count")
    override val minCount: Int?,
    @field:PositiveOrZero
    @JsonProperty("max_count")
    override val maxCount: Int?,
    override val path: ThingId,
    val datatype: ThingId,
) : TemplatePropertyRequest {
    override fun toTemplatePropertyCommand(): TemplatePropertyCommand =
        OtherLiteralPropertyCommand(label, placeholder, description, minCount, maxCount, path, datatype)
}

data class ResourcePropertyRequest(
    override val label: String,
    override val placeholder: String?,
    override val description: String?,
    @field:PositiveOrZero
    @JsonProperty("min_count")
    override val minCount: Int?,
    @field:PositiveOrZero
    @JsonProperty("max_count")
    override val maxCount: Int?,
    override val path: ThingId,
    val `class`: ThingId,
) : TemplatePropertyRequest {
    override fun toTemplatePropertyCommand(): TemplatePropertyCommand =
        ResourcePropertyCommand(label, placeholder, description, minCount, maxCount, path, `class`)
}

data class IdentifierMapRequest(
    val values: Map<String, List<String>>,
)

data class CreateRowRequest(
    @field:NullableNotBlank
    val label: String?,
    val data: List<String?>,
) {
    fun toCreateRowCommand(): CreateRowCommand =
        CreateRowCommand(label, data)
}

data class UpdateRowRequest(
    @field:NullableNotBlank
    val label: String?,
    val data: List<String?>,
) {
    fun toUpdateRowCommand(): UpdateRowCommand =
        UpdateRowCommand(label, data)
}
