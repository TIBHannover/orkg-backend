package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.graph.input.MarkAsVerifiedUseCase
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
class PaperVerificationCommandController(
    private val service: MarkAsVerifiedUseCase
) {
    @PutMapping("/{id}/metadata/verified")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markVerified(@PathVariable id: ThingId) {
        service.markAsVerified(id)
    }

    @DeleteMapping("/{id}/metadata/verified")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unmarkVerified(@PathVariable id: ThingId) {
        service.markAsUnverified(id)
    }
}
