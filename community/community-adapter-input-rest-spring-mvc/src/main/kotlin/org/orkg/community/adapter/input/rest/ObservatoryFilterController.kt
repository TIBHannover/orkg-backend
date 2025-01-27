package org.orkg.community.adapter.input.rest

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.common.exceptions.Forbidden
import org.orkg.community.adapter.input.rest.mapping.ObservatoryFilterRepresentationAdapter
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.community.domain.ObservatoryFilterNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.input.CreateObservatoryFilterUseCase.CreateCommand
import org.orkg.community.input.ObservatoryFilterUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.input.UpdateObservatoryFilterUseCase
import org.orkg.graph.domain.PredicatePath
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/observatories", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObservatoryFilterController(
    private val observatoryService: ObservatoryUseCases,
    private val service: ObservatoryFilterUseCases,
    private val contributorService: RetrieveContributorUseCase
) : ObservatoryFilterRepresentationAdapter {
    @RequireLogin
    @PostMapping("/{id}/filters", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @PathVariable(name = "id") id: ObservatoryId,
        @RequestBody @Valid request: CreateObservatoryFilterRequest,
        currentUser: Authentication,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ObservatoryFilterRepresentation> {
        val contributorId = currentUser.contributorId()
        authorizeUser(contributorId, id)
        val filterId = service.create(request.toCreateCommand(id, contributorId))
        val location = uriComponentsBuilder
            .path("/api/observatories/{id}/filters/{filterId}")
            .buildAndExpand(id, filterId)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PatchMapping("/{id}/filters/{filterId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable(name = "id") id: ObservatoryId,
        @PathVariable(name = "filterId") filterId: ObservatoryFilterId,
        @RequestBody @Valid request: UpdateObservatoryFilterRequest,
        currentUser: Authentication,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        observatoryService.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
        authorizeUser(currentUser.contributorId(), id)
        service.update(request.toUpdateCommand(filterId))
        val location = uriComponentsBuilder
            .path("/api/observatories/{id}/filters/{filterId}")
            .buildAndExpand(id, filterId)
            .toUri()
        return noContent().location(location).build()
    }

    @GetMapping("/{id}/filters/{filterId}")
    fun findById(
        @PathVariable(name = "id") id: ObservatoryId,
        @PathVariable(name = "filterId") filterId: ObservatoryFilterId
    ): ObservatoryFilterRepresentation {
        observatoryService.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
        return service.findById(filterId)
            .mapToObservatoryFilterRepresentation()
            .orElseThrow { ObservatoryFilterNotFound(filterId) }
    }

    @GetMapping("/{id}/filters")
    fun findAllByObservatoryId(
        @PathVariable(name = "id") id: ObservatoryId,
        pageable: Pageable
    ): Page<ObservatoryFilterRepresentation> {
        observatoryService.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
        return service.findAllByObservatoryId(id, pageable)
            .mapToObservatoryFilterRepresentation()
    }

    @RequireLogin
    @DeleteMapping("/{id}/filters/{filterId}")
    fun deleteById(
        @PathVariable(name = "id") id: ObservatoryId,
        @PathVariable(name = "filterId") filterId: ObservatoryFilterId,
        currentUser: Authentication,
    ): ResponseEntity<Any> {
        authorizeUser(currentUser.contributorId(), id)
        observatoryService.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
        service.deleteById(filterId)
        return noContent().build()
    }

    private fun authorizeUser(contributorId: ContributorId, observatoryId: ObservatoryId) {
        val user = contributorService.findById(contributorId)
            .orElseThrow { ContributorNotFound(contributorId) }
        if (user.observatoryId != observatoryId && !user.isAdmin) {
            throw Forbidden()
        }
    }

    data class CreateObservatoryFilterRequest(
        val label: String,
        @field:Valid
        @field:Size(min = 1)
        val path: PredicatePath,
        val range: ThingId,
        val exact: Boolean,
        val featured: Boolean = false
    ) {
        fun toCreateCommand(observatoryId: ObservatoryId, contributorId: ContributorId) =
            CreateCommand(
                observatoryId = observatoryId,
                label = label,
                contributorId = contributorId,
                path = path,
                range = range,
                exact = exact,
                featured = featured
            )
    }

    data class UpdateObservatoryFilterRequest(
        val label: String?,
        @field:Valid
        @field:Size(min = 1)
        val path: PredicatePath?,
        val range: ThingId?,
        val exact: Boolean?,
        val featured: Boolean?
    ) {
        fun toUpdateCommand(id: ObservatoryFilterId) =
            UpdateObservatoryFilterUseCase.UpdateCommand(
                id = id,
                label = label,
                path = path,
                range = range,
                exact = exact,
                featured = featured
            )
    }
}
