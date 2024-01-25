package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import javax.validation.constraints.NotBlank
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ClassDefinition
import org.orkg.contenttypes.input.ListDefinition
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
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
