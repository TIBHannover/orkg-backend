package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod

interface CreateContributionUseCase {
    fun create(command: CreateCommand): ThingId

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
