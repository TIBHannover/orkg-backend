package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ClassDefinition
import org.orkg.contenttypes.input.ListDefinition
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.NumberLiteralPropertyDefinition
import org.orkg.contenttypes.input.OtherLiteralPropertyDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.ResourcePropertyDefinition
import org.orkg.contenttypes.input.StringLiteralPropertyDefinition
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.contenttypes.input.UntypedPropertyDefinition
import org.orkg.graph.domain.Literals

data class ListDefinitionDTO(
    @field:NotBlank
    val label: String,
    val elements: List<String>
) {
    fun toCreateCommand(): ListDefinition =
        ListDefinition(
            label = label,
            elements = elements
        )
}

data class LiteralDefinitionDTO(
    @field:NotBlank
    val label: String,
    @JsonProperty("data_type")
    val dataType: String?
) {
    fun toCreateCommand(): LiteralDefinition =
        LiteralDefinition(
            label = label,
            dataType = dataType ?: Literals.XSD.STRING.prefixedUri
        )
}

data class PredicateDefinitionDTO(
    @field:NotBlank
    val label: String,
    @field:NotBlank
    val description: String?
) {
    fun toCreateCommand(): PredicateDefinition =
        PredicateDefinition(
            label = label,
            description = description
        )
}

data class ResourceDefinitionDTO(
    @field:NotBlank
    val label: String,
    val classes: Set<ThingId>?
) {
    fun toCreateCommand(): ResourceDefinition =
        ResourceDefinition(
            label = label,
            classes = classes.orEmpty()
        )
}

data class ClassDefinitionDTO(
    @field:NotBlank
    val label: String,
    val uri: URI? = null
) {
    fun toCreateCommand(): ClassDefinition =
        ClassDefinition(
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

    fun toTemplatePropertyDefinition(): TemplatePropertyDefinition
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
    override val path: ThingId
) : TemplatePropertyRequest {
    override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
        UntypedPropertyDefinition(label, placeholder, description, minCount, maxCount, path)
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
    val datatype: ThingId
) : TemplatePropertyRequest {
    override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
        StringLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, pattern, path, datatype)
}

data class NumberLiteralPropertyRequest<T : Number>(
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
    val minInclusive: T?,
    @JsonProperty("max_inclusive")
    val maxInclusive: T?,
    override val path: ThingId,
    val datatype: ThingId
) : TemplatePropertyRequest {
    override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
        NumberLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, minInclusive, maxInclusive, path, datatype)
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
    val datatype: ThingId
) : TemplatePropertyRequest {
    override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
        OtherLiteralPropertyDefinition(label, placeholder, description, minCount, maxCount, path, datatype)
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
    val `class`: ThingId
) : TemplatePropertyRequest {
    override fun toTemplatePropertyDefinition(): TemplatePropertyDefinition =
        ResourcePropertyDefinition(label, placeholder, description, minCount, maxCount, path, `class`)
}
