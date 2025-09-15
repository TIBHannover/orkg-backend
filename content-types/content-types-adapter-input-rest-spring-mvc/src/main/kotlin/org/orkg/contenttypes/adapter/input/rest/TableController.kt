package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.TableRepresentationAdapter
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.input.CreateTableRowUseCase
import org.orkg.contenttypes.input.CreateTableUseCase
import org.orkg.contenttypes.input.DeleteTableRowUseCase
import org.orkg.contenttypes.input.TableUseCases
import org.orkg.contenttypes.input.UpdateTableRowUseCase
import org.orkg.contenttypes.input.UpdateTableUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.time.OffsetDateTime

const val TABLE_JSON_V1 = "application/vnd.orkg.table.v1+json"
const val TABLE_ROW_JSON_V1 = "application/vnd.orkg.table.row.v1+json"

@RestController
@RequestMapping("/api/tables", produces = [TABLE_JSON_V1])
class TableController(
    private val service: TableUseCases,
) : TableRepresentationAdapter {
    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: ThingId,
    ): TableRepresentation = service.findById(id)
        .mapToTableRepresentation()
        .orElseThrow { TableNotFound(id) }

    @GetMapping
    fun findAll(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        @RequestParam("created_by", required = false) createdBy: ContributorId?,
        @RequestParam("created_at_start", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtStart: OffsetDateTime?,
        @RequestParam("created_at_end", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) createdAtEnd: OffsetDateTime?,
        @RequestParam("observatory_id", required = false) observatoryId: ObservatoryId?,
        @RequestParam("organization_id", required = false) organizationId: OrganizationId?,
        pageable: Pageable,
    ): Page<TableRepresentation> =
        service.findAll(
            pageable = pageable,
            label = string?.let { SearchString.of(string, exactMatch) },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId
        ).mapToTableRepresentation()

    @RequireLogin
    @PostMapping(consumes = [TABLE_JSON_V1])
    fun create(
        @RequestBody @Valid request: CreateTableRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val id = service.create(request.toCreateCommand(userId))
        val location = uriComponentsBuilder
            .path("/api/tables/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}", consumes = [TABLE_JSON_V1])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateTableRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/tables/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @PostMapping(path = ["/{id}/rows", "/{id}/rows/{index}"], consumes = [TABLE_ROW_JSON_V1], produces = [TABLE_ROW_JSON_V1])
    fun createTableRow(
        @PathVariable id: ThingId,
        @PathVariable(required = false) @PositiveOrZero index: Int?,
        @RequestBody @Valid request: TableRowRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.createTableRow(request.toCreateCommand(id, userId, index))
        val location = uriComponentsBuilder
            .path("/api/tables/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}/rows/{index}", consumes = [TABLE_ROW_JSON_V1], produces = [TABLE_ROW_JSON_V1])
    fun updateTableRow(
        @PathVariable id: ThingId,
        @PathVariable @PositiveOrZero index: Int,
        @RequestBody @Valid request: TableRowRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.updateTableRow(request.toUpdateCommand(id, userId, index))
        val location = uriComponentsBuilder
            .path("/api/tables/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @DeleteMapping("/{id}/rows/{index}", consumes = [TABLE_ROW_JSON_V1], produces = [TABLE_ROW_JSON_V1])
    fun deleteTableRow(
        @PathVariable id: ThingId,
        @PathVariable @PositiveOrZero index: Int,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.deleteTableRow(DeleteTableRowUseCase.DeleteCommand(id, userId, index))
        val location = uriComponentsBuilder
            .path("/api/tables/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateTableRequest(
        @field:NotBlank
        val label: String,
        @field:Valid
        val resources: Map<String, CreateResourceRequestPart>?,
        @field:Valid
        val literals: Map<String, CreateLiteralRequestPart>?,
        @field:Valid
        val predicates: Map<String, CreatePredicateRequestPart>?,
        @field:Valid
        val classes: Map<String, CreateClassRequestPart>?,
        @field:Valid
        val lists: Map<String, CreateListRequestPart>?,
        @field:Valid
        @field:Size(min = 2)
        val rows: List<RowRequest>,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    ) {
        fun toCreateCommand(contributorId: ContributorId): CreateTableUseCase.CreateCommand =
            CreateTableUseCase.CreateCommand(
                contributorId = contributorId,
                label = label,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                classes = classes?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                rows = rows.map { it.toRowCommand() },
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod
            )
    }

    data class UpdateTableRequest(
        @field:NullableNotBlank
        val label: String?,
        @field:Valid
        val resources: Map<String, CreateResourceRequestPart>?,
        @field:Valid
        val literals: Map<String, CreateLiteralRequestPart>?,
        @field:Valid
        val predicates: Map<String, CreatePredicateRequestPart>?,
        @field:Valid
        val classes: Map<String, CreateClassRequestPart>?,
        @field:Valid
        val lists: Map<String, CreateListRequestPart>?,
        @field:Valid
        @field:Size(min = 2)
        val rows: List<RowRequest>?,
        @field:Size(max = 1)
        val observatories: List<ObservatoryId>?,
        @field:Size(max = 1)
        val organizations: List<OrganizationId>?,
        @JsonProperty("extraction_method")
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
    ) {
        fun toUpdateCommand(tableId: ThingId, contributorId: ContributorId): UpdateTableUseCase.UpdateCommand =
            UpdateTableUseCase.UpdateCommand(
                tableId = tableId,
                contributorId = contributorId,
                label = label,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                classes = classes?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                rows = rows?.map { it.toRowCommand() },
                observatories = observatories,
                organizations = organizations,
                extractionMethod = extractionMethod,
                visibility = visibility
            )
    }

    data class TableRowRequest(
        @field:Valid
        val resources: Map<String, CreateResourceRequestPart>?,
        @field:Valid
        val literals: Map<String, CreateLiteralRequestPart>?,
        @field:Valid
        val predicates: Map<String, CreatePredicateRequestPart>?,
        @field:Valid
        val classes: Map<String, CreateClassRequestPart>?,
        @field:Valid
        val lists: Map<String, CreateListRequestPart>?,
        @field:Valid
        val row: RowRequest,
    ) {
        fun toCreateCommand(tableId: ThingId, contributorId: ContributorId, rowIndex: Int?): CreateTableRowUseCase.CreateCommand =
            CreateTableRowUseCase.CreateCommand(
                tableId = tableId,
                contributorId = contributorId,
                rowIndex = rowIndex,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                classes = classes?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                row = row.toRowCommand(),
            )

        fun toUpdateCommand(tableId: ThingId, contributorId: ContributorId, rowIndex: Int): UpdateTableRowUseCase.UpdateCommand =
            UpdateTableRowUseCase.UpdateCommand(
                tableId = tableId,
                contributorId = contributorId,
                rowIndex = rowIndex,
                resources = resources?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                literals = literals?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                predicates = predicates?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                classes = classes?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                lists = lists?.mapValues { it.value.toCreateCommand() }.orEmpty(),
                row = row.toRowCommand(),
            )
    }
}
