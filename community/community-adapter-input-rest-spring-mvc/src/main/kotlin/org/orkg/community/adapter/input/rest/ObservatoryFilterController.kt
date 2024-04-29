package org.orkg.community.adapter.input.rest

import javax.validation.Valid
import javax.validation.constraints.Size
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.common.contributorId
import org.orkg.common.exceptions.Forbidden
import org.orkg.community.adapter.input.rest.mapping.ObservatoryFilterRepresentationAdapter
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.community.domain.ObservatoryFilterNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.input.CreateObservatoryFilterUseCase.CreateCommand
import org.orkg.community.input.ObservatoryFilterUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.input.UpdateObservatoryFilterUseCase
import org.orkg.community.output.AdminRepository
import org.orkg.graph.domain.ContributorNotFound
import org.orkg.graph.domain.PredicatePath
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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
    private val contributorService: RetrieveContributorUseCase,
    private val adminRepository: AdminRepository
) : ObservatoryFilterRepresentationAdapter {
    @PostMapping("/{id}/filters", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @PathVariable(name = "id") observatoryId: ObservatoryId,
        @RequestBody @Valid request: CreateObservatoryFilterRequest,
        @AuthenticationPrincipal currentUser: UserDetails,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<ObservatoryFilterRepresentation> {
        val contributorId = currentUser.contributorId()
        authorizeUser(contributorId, observatoryId)
        val id = service.create(request.toCreateCommand(observatoryId, contributorId))
        val location = uriComponentsBuilder
            .path("api/observatories/{observatoryId}/filters/{id}")
            .buildAndExpand(observatoryId, id)
            .toUri()
        return created(location).build()
    }

    @PatchMapping("/{observatoryId}/filters/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable(name = "observatoryId") observatoryId: ObservatoryId,
        @PathVariable(name = "id") id: ObservatoryFilterId,
        @RequestBody @Valid request: UpdateObservatoryFilterRequest,
        @AuthenticationPrincipal currentUser: UserDetails,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        observatoryService.findById(observatoryId)
            .orElseThrow { ObservatoryNotFound(observatoryId) }
        authorizeUser(currentUser.contributorId(), observatoryId)
        service.update(request.toUpdateCommand(id))
        return noContent().build()
    }

    @GetMapping("/{observatoryId}/filters/{id}")
    fun findById(
        @PathVariable(name = "observatoryId") observatoryId: ObservatoryId,
        @PathVariable(name = "id") id: ObservatoryFilterId
    ): ObservatoryFilterRepresentation {
        observatoryService.findById(observatoryId)
            .orElseThrow { ObservatoryNotFound(observatoryId) }
        return service.findById(id)
            .mapToObservatoryFilterRepresentation()
            .orElseThrow { ObservatoryFilterNotFound(id) }
    }

    @GetMapping("/{observatoryId}/filters")
    fun findAllByObservatoryId(
        @PathVariable(name = "observatoryId") id: ObservatoryId,
        pageable: Pageable
    ): Page<ObservatoryFilterRepresentation> {
        observatoryService.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
        return service.findAllByObservatoryId(id, pageable)
            .mapToObservatoryFilterRepresentation()
    }

    @DeleteMapping("/{observatoryId}/filters/{id}")
    fun deleteById(
        @PathVariable(name = "observatoryId") observatoryId: ObservatoryId,
        @PathVariable(name = "id") id: ObservatoryFilterId,
        @AuthenticationPrincipal currentUser: UserDetails,
    ): ResponseEntity<Any> {
        authorizeUser(currentUser.contributorId(), observatoryId)
        observatoryService.findById(observatoryId)
            .orElseThrow { ObservatoryNotFound(observatoryId) }
        service.deleteById(id)
        return noContent().build()
    }

    private fun authorizeUser(contributorId: ContributorId, observatoryId: ObservatoryId) {
        val user = contributorService.findById(contributorId)
            .orElseThrow { ContributorNotFound(contributorId) }
        if (user.observatoryId != observatoryId && !adminRepository.hasAdminPriviledges(contributorId)) {
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
