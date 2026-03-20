package org.orkg.contenttypes.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonTableCsvAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonTableRepresentationAdapter
import org.orkg.contenttypes.domain.ComparisonTableNotFound
import org.orkg.contenttypes.domain.SimpleComparisonPath
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.contenttypes.input.UpdateComparisonTableUseCase
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/comparisons", produces = [COMPARISON_JSON_V3])
class ComparisonTableController(
    private val service: ComparisonTableUseCases,
) : ComparisonTableRepresentationAdapter,
    ComparisonTableCsvAdapter {
    @GetMapping("/{id}/table-paths")
    fun findAllComparisonTablePredicatePathsByComparisonId(
        @PathVariable id: ThingId,
    ): List<LabeledComparisonPathRepresentation> =
        service.findAllPathsByComparisonId(id)
            .mapToLabeledComparisonPathRepresentation()

    @GetMapping("/{id}/contents")
    fun findByComparisonId(
        @PathVariable id: ThingId,
    ): ComparisonTableRepresentation =
        service.findByComparisonId(id)
            .mapToComparisonTableRepresentation()
            .orElseThrow { ComparisonTableNotFound(id) }

    @GetMapping("/{id}/contents", produces = ["text/csv"])
    fun findByComparisonIdAsCsv(
        @PathVariable id: ThingId,
    ): String =
        service.findByComparisonId(id)
            .mapToComparisonTableCsv()
            .orElseThrow { ComparisonTableNotFound(id) }

    @RequireLogin
    @PutMapping("/{id}/contents", consumes = [COMPARISON_JSON_V3])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: UpdateComparisonTableRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}/contents")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class UpdateComparisonTableRequest(
        @param:JsonProperty("selected_paths")
        val selectedPaths: List<SimpleComparisonPath>,
    ) {
        fun toUpdateCommand(comparisonId: ThingId, contributorId: ContributorId) =
            UpdateComparisonTableUseCase.UpdateCommand(comparisonId, contributorId, selectedPaths)
    }
}
