package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Certainty
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

interface CreateRosettaStoneStatementUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val templateId: ThingId,
        val contributorId: ContributorId,
        val context: ThingId?,
        val subjects: List<String>,
        val objects: List<List<String>>,
        val certainty: Certainty,
        val negated: Boolean,
        val extractionMethod: ExtractionMethod,
        override val resources: Map<String, ResourceDefinition> = emptyMap(),
        override val literals: Map<String, LiteralDefinition> = emptyMap(),
        override val predicates: Map<String, PredicateDefinition> = emptyMap(),
        override val classes: Map<String, ClassDefinition> = emptyMap(),
        override val lists: Map<String, ListDefinition> = emptyMap(),
        val observatories: List<ObservatoryId> = emptyList(),
        val organizations: List<OrganizationId> = emptyList(),
        val visibility: Visibility = Visibility.DEFAULT,
        val modifiable: Boolean = true
    ) : ThingDefinitions
}

interface UpdateRosettaStoneStatementUseCase {
    fun update(command: UpdateCommand): ThingId

    data class UpdateCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val subjects: List<String>,
        val objects: List<List<String>>,
        val certainty: Certainty,
        val negated: Boolean,
        val extractionMethod: ExtractionMethod,
        override val resources: Map<String, ResourceDefinition> = emptyMap(),
        override val literals: Map<String, LiteralDefinition> = emptyMap(),
        override val predicates: Map<String, PredicateDefinition> = emptyMap(),
        override val classes: Map<String, ClassDefinition> = emptyMap(),
        override val lists: Map<String, ListDefinition> = emptyMap(),
        val observatories: List<ObservatoryId> = emptyList(),
        val organizations: List<OrganizationId> = emptyList(),
        val visibility: Visibility = Visibility.DEFAULT,
        val modifiable: Boolean = true
    ) : ThingDefinitions
}
