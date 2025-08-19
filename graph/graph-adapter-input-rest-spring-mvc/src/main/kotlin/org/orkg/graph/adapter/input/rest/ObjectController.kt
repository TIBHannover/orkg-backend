package org.orkg.graph.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireLogin
import org.orkg.common.contributorId
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.CreateObjectUseCase.CreateObjectRequest
import org.orkg.graph.input.ObjectUseCases
import org.orkg.graph.input.ResourceUseCases
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/objects", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObjectController(
    private val resourceService: ResourceUseCases,
    private val objectService: ObjectUseCases,
) {
    @RequireLogin
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<ResourceRepresentation> {
        val id = objectService.createObject(obj, null, currentUser.contributorId().value)
        val location = uriComponentsBuilder
            .path("/api/resources/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @RequireLogin
    @PatchMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun add(
        @PathVariable id: ThingId,
        @RequestBody obj: CreateObjectRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<ResourceRepresentation> {
        resourceService.findById(id)
            .orElseThrow { ResourceNotFound(id) }
        objectService.createObject(obj, id, currentUser.contributorId().value)
        val location = uriComponentsBuilder
            .path("/api/resources/{id}")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }
}
