package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.graph.input.ResourceUseCases
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/papers", produces = [MediaType.APPLICATION_JSON_VALUE])
class PaperFeaturedCommandController(
    private val service: ResourceUseCases
) {
    @PutMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markFeatured(@PathVariable id: ThingId) {
        service.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    fun unmarkFeatured(@PathVariable id: ThingId) {
        service.markAsNonFeatured(id)
    }
}
