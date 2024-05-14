package org.orkg.contenttypes.input

import java.net.URI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literals

data class PublicationInfoDefinition(
    val publishedMonth: Int?,
    val publishedYear: Long?,
    val publishedIn: String?,
    val url: URI?
)

sealed interface ThingDefinitions {
    val resources: Map<String, ResourceDefinition>
    val literals: Map<String, LiteralDefinition>
    val predicates: Map<String, PredicateDefinition>
    val classes: Map<String, ClassDefinition>
    val lists: Map<String, ListDefinition>

    fun all(): Map<String, ThingDefinition> =
        resources + literals + predicates + classes + lists
}

sealed interface ThingDefinition {
    val label: String
}

data class ResourceDefinition(
    override val label: String,
    val classes: Set<ThingId> = emptySet()
) : ThingDefinition

data class ClassDefinition(
    override val label: String,
    val uri: URI? = null
) : ThingDefinition

data class ListDefinition(
    override val label: String,
    val elements: List<String> = emptyList()
) : ThingDefinition

data class LiteralDefinition(
    override val label: String,
    val dataType: String = Literals.XSD.STRING.prefixedUri
) : ThingDefinition

data class PredicateDefinition(
    override val label: String,
    val description: String? = null
) : ThingDefinition
