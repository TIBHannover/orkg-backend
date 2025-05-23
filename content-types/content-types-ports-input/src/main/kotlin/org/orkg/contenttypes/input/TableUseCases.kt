package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface TableUseCases :
    RetrieveTableUseCase,
    CreateTableUseCase,
    UpdateTableUseCase

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
        override val resources: Map<String, CreateResourceCommandPart>,
        override val literals: Map<String, CreateLiteralCommandPart>,
        override val predicates: Map<String, CreatePredicateCommandPart>,
        override val classes: Map<String, CreateClassCommandPart>,
        override val lists: Map<String, CreateListCommandPart>,
        val rows: List<RowCommand>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val extractionMethod: ExtractionMethod,
    ) : CreateThingsCommand
}

interface UpdateTableUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val tableId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        override val resources: Map<String, CreateResourceCommandPart>,
        override val literals: Map<String, CreateLiteralCommandPart>,
        override val predicates: Map<String, CreatePredicateCommandPart>,
        override val classes: Map<String, CreateClassCommandPart>,
        override val lists: Map<String, CreateListCommandPart>,
        val rows: List<RowCommand>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
    ) : CreateThingsCommand
}

data class RowCommand(
    val label: String?,
    val data: List<String?>,
) {
    fun matchesRow(row: Table.Row): Boolean =
        row.label == label && matchesRowData(row)

    fun matchesRowData(row: Table.Row): Boolean =
        data.size == row.data.size && data.zip(row.data).all { (a, b) -> a == b?.id?.value }
}
