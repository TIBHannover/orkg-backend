package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ThingDefinitions.ListDefinition
import org.orkg.contenttypes.input.ThingDefinitions.LiteralDefinition
import org.orkg.contenttypes.input.ThingDefinitions.PredicateDefinition
import org.orkg.contenttypes.input.ThingDefinitions.ResourceDefinition
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
    ) : ThingDefinitions
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
    val lists: Map<String, ListDefinition>

    data class ResourceDefinition(
        val label: String,
        val classes: Set<ThingId> = emptySet()
    )

    data class LiteralDefinition(
        val label: String,
        val dataType: String = Literals.XSD.STRING.prefixedUri
    )

    data class PredicateDefinition(
        val label: String,
        val description: String? = null
    )

    data class ListDefinition(
        val label: String,
        val elements: List<String> = emptyList()
    )
}
