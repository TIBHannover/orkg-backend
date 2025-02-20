package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.contributorId
import org.orkg.graph.input.ResourceUseCases
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contributions", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyContributionController(
    private val resourceService: ResourceUseCases,
) {
    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    @RequireCuratorRole
    fun markFeatured(
        @PathVariable id: ThingId,
    ) {
        resourceService.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    fun unmarkFeatured(
        @PathVariable id: ThingId,
    ) {
        resourceService.markAsNonFeatured(id)
    }

    @PutMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(
        @PathVariable id: ThingId,
        currentUser: Authentication?,
    ) {
        resourceService.markAsUnlisted(id, currentUser.contributorId())
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    fun unmarkUnlisted(
        @PathVariable id: ThingId,
    ) {
        resourceService.markAsListed(id)
    }
}
