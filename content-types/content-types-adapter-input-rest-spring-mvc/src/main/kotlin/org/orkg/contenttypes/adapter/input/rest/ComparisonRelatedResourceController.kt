package org.orkg.contenttypes.adapter.input.rest

import jakarta.validation.Valid
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRelatedResourceRepresentationAdapter
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.input.ComparisonRelatedResourceUseCases
import org.orkg.contenttypes.input.CreateComparisonRelatedResourceUseCase
import org.orkg.contenttypes.input.UpdateComparisonRelatedResourceUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/comparisons", produces = [COMPARISON_JSON_V2])
class ComparisonRelatedResourceController(
    private val service: ComparisonRelatedResourceUseCases,
) : ComparisonRelatedResourceRepresentationAdapter {
    @GetMapping("/{id}/related-resources/{comparisonRelatedResourceId}")
    fun findByIdAndComparisonId(
        @PathVariable id: ThingId,
        @PathVariable comparisonRelatedResourceId: ThingId,
    ): ComparisonRelatedResourceRepresentation =
        service.findByIdAndComparisonId(id, comparisonRelatedResourceId)
            .mapToComparisonRelatedResourceRepresentation()
            .orElseThrow { ComparisonRelatedResourceNotFound(comparisonRelatedResourceId) }

    @GetMapping("/{id}/related-resources")
    fun findAllByComparisonId(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): Page<ComparisonRelatedResourceRepresentation> =
        service.findAllByComparisonId(id, pageable)
            .mapToComparisonRelatedResourceRepresentation()

    @RequireLogin
    @PostMapping("/{id}/related-resources", consumes = [COMPARISON_JSON_V2])
    fun create(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: CreateComparisonRelatedResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val comparisonRelatedResourceId = service.create(request.toCreateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}")
            .buildAndExpand(id, comparisonRelatedResourceId)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}/related-resources/{comparisonRelatedResourceId}", consumes = [COMPARISON_JSON_V2])
    fun update(
        @PathVariable id: ThingId,
        @PathVariable comparisonRelatedResourceId: ThingId,
        @RequestBody @Valid request: UpdateComparisonRelatedResourceRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, comparisonRelatedResourceId, userId))
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}")
            .buildAndExpand(id, comparisonRelatedResourceId)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @DeleteMapping("/{id}/related-resources/{comparisonRelatedResourceId}")
    fun deleteByIdAndComparisonId(
        @PathVariable id: ThingId,
        @PathVariable comparisonRelatedResourceId: ThingId,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.deleteByIdAndComparisonId(id, comparisonRelatedResourceId, userId)
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateComparisonRelatedResourceRequest(
        val label: String,
        @field:NullableNotBlank
        val image: String?,
        @field:NullableNotBlank
        val url: String?,
        @field:NullableNotBlank
        val description: String?,
    ) {
        fun toCreateCommand(comparisonId: ThingId, contributorId: ContributorId) =
            CreateComparisonRelatedResourceUseCase.CreateCommand(
                comparisonId = comparisonId,
                contributorId = contributorId,
                label = label,
                image = image,
                url = url,
                description = description
            )
    }

    data class UpdateComparisonRelatedResourceRequest(
        val label: String?,
        @field:NullableNotBlank
        val image: String?,
        @field:NullableNotBlank
        val url: String?,
        @field:NullableNotBlank
        val description: String?,
    ) {
        fun toUpdateCommand(comparisonId: ThingId, comparisonRelatedResourceId: ThingId, contributorId: ContributorId) =
            UpdateComparisonRelatedResourceUseCase.UpdateCommand(
                comparisonId = comparisonId,
                comparisonRelatedResourceId = comparisonRelatedResourceId,
                contributorId = contributorId,
                label = label,
                image = image,
                url = url,
                description = description
            )
    }
}
