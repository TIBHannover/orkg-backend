package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod

interface UpdateTemplateInstanceUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val subject: ThingId,
        val templateId: ThingId,
        val contributorId: ContributorId,
        val statements: Map<ThingId, List<String>>,
        override val resources: Map<String, ResourceDefinition> = emptyMap(),
        override val literals: Map<String, LiteralDefinition> = emptyMap(),
        override val predicates: Map<String, PredicateDefinition> = emptyMap(),
        override val classes: Map<String, ClassDefinition> = emptyMap(),
        override val lists: Map<String, ListDefinition> = emptyMap(),
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN
    ) : ThingDefinitions
}
