package org.orkg.contenttypes.adapter.input.rest

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.validation.NullableNotBlank
import org.orkg.contenttypes.adapter.input.rest.mapping.ComparisonRelatedFigureRepresentationAdapter
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.input.ComparisonRelatedFigureUseCases
import org.orkg.contenttypes.input.CreateComparisonRelatedFigureUseCase
import org.orkg.contenttypes.input.UpdateComparisonRelatedFigureUseCase
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
class ComparisonRelatedFigureController(
    private val service: ComparisonRelatedFigureUseCases,
) : ComparisonRelatedFigureRepresentationAdapter {
    @GetMapping("/{id}/related-figures/{comparisonRelatedFigureId}")
    fun findByIdAndComparisonId(
        @PathVariable id: ThingId,
        @PathVariable comparisonRelatedFigureId: ThingId,
    ): ComparisonRelatedFigureRepresentation =
        service.findByIdAndComparisonId(id, comparisonRelatedFigureId)
            .mapToComparisonRelatedFigureRepresentation()
            .orElseThrow { ComparisonRelatedFigureNotFound(comparisonRelatedFigureId) }

    @GetMapping("/{id}/related-figures")
    fun findAllByComparisonId(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): Page<ComparisonRelatedFigureRepresentation> =
        service.findAllByComparisonId(id, pageable)
            .mapToComparisonRelatedFigureRepresentation()

    @RequireLogin
    @PostMapping("/{id}/related-figures", consumes = [COMPARISON_JSON_V2])
    fun create(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: CreateComparisonRelatedFigureRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        val comparisonRelatedFigureId = service.create(request.toCreateCommand(id, userId))
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}")
            .buildAndExpand(id, comparisonRelatedFigureId)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PutMapping("/{id}/related-figures/{comparisonRelatedFigureId}", consumes = [COMPARISON_JSON_V2])
    fun update(
        @PathVariable id: ThingId,
        @PathVariable comparisonRelatedFigureId: ThingId,
        @RequestBody @Valid request: UpdateComparisonRelatedFigureRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.update(request.toUpdateCommand(id, comparisonRelatedFigureId, userId))
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}")
            .buildAndExpand(id, comparisonRelatedFigureId)
            .toUri()
        return noContent().location(location).build()
    }

    @RequireLogin
    @DeleteMapping("/{id}/related-figures/{comparisonRelatedFigureId}")
    fun delete(
        @PathVariable id: ThingId,
        @PathVariable comparisonRelatedFigureId: ThingId,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        val userId = currentUser.contributorId()
        service.delete(id, comparisonRelatedFigureId, userId)
        val location = uriComponentsBuilder
            .path("/api/comparisons/{id}")
            .buildAndExpand(id)
            .toUri()
        return noContent().location(location).build()
    }

    data class CreateComparisonRelatedFigureRequest(
        val label: String,
        @field:NullableNotBlank
        val image: String?,
        @field:NullableNotBlank
        val description: String?,
    ) {
        fun toCreateCommand(comparisonId: ThingId, contributorId: ContributorId) =
            CreateComparisonRelatedFigureUseCase.CreateCommand(
                comparisonId = comparisonId,
                contributorId = contributorId,
                label = label,
                image = image,
                description = description
            )
    }

    data class UpdateComparisonRelatedFigureRequest(
        val label: String?,
        @field:NotBlank
        val image: String,
        @field:NotBlank
        val description: String,
    ) {
        fun toUpdateCommand(comparisonId: ThingId, comparisonRelatedFigureId: ThingId, contributorId: ContributorId) =
            UpdateComparisonRelatedFigureUseCase.UpdateCommand(
                comparisonId = comparisonId,
                comparisonRelatedFigureId = comparisonRelatedFigureId,
                contributorId = contributorId,
                label = label,
                image = image,
                description = description
            )
    }
}
