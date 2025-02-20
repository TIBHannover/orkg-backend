package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.contributorId
import org.orkg.graph.input.MarkAsUnlistedService
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
@RequestMapping("/api/papers", produces = [MediaType.APPLICATION_JSON_VALUE])
class PaperUnlistedCommandController(
    private val service: MarkAsUnlistedService,
) {
    @PutMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markUnlisted(
        @PathVariable id: ThingId,
        currentUser: Authentication?,
    ) {
        service.markAsUnlisted(id, currentUser.contributorId())
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    fun markListed(
        @PathVariable id: ThingId,
    ) {
        service.markAsListed(id)
    }
}
