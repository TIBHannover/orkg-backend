package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface TableUseCases :
    RetrieveTableUseCase,
    CreateTableUseCase

interface RetrieveTableUseCase {
    fun findById(id: ThingId): Optional<Table>

    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
    ): Page<Table>
}

interface CreateTableUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val label: String,
        override val resources: Map<String, ResourceDefinition>,
        override val literals: Map<String, LiteralDefinition>,
        override val predicates: Map<String, PredicateDefinition>,
        override val classes: Map<String, ClassDefinition>,
        override val lists: Map<String, ListDefinition>,
        val rows: List<RowDefinition>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val extractionMethod: ExtractionMethod,
    ) : ThingDefinitions
}

data class RowDefinition(
    val label: String?,
    val data: List<String?>,
)
