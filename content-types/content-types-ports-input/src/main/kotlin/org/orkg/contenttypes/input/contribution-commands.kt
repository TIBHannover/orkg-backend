package org.orkg.contenttypes.input

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals

interface CreateContributionUseCase {
    fun createContribution(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val paperId: ThingId,
        val extractionMethod: ExtractionMethod,
        override val resources: Map<String, ResourceDefinition> = emptyMap(),
        override val literals: Map<String, LiteralDefinition> = emptyMap(),
        override val predicates: Map<String, PredicateDefinition> = emptyMap(),
        override val lists: Map<String, ListDefinition> = emptyMap(),
        val contribution: ContributionDefinition
    ) : ThingDefinitions {
        override val classes: Map<String, ClassDefinition>
            get() = emptyMap()
    }
}

data class ContributionDefinition(
    val label: String,
    val classes: Set<ThingId> = emptySet(),
    val statements: Map<String, List<StatementObjectDefinition>>
) {
    data class StatementObjectDefinition(
        val id: String,
        val statements: Map<String, List<StatementObjectDefinition>>? = null
    )
}

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
