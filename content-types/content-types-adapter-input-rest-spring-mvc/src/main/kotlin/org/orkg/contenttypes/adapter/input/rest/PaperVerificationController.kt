package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.GetPaperFlagQuery
import org.orkg.contenttypes.input.LoadPaperPort
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceNotFound
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PaperVerificationController(
    private val port: LoadPaperPort,
    private val query: GetPaperFlagQuery
) {

    @GetMapping("/api/papers/{id}/metadata/verified")
    fun getVerifiedFlag(@PathVariable id: ThingId): Boolean =
        query.getPaperVerifiedFlag(id) ?: throw ResourceNotFound.withId(id)

    @GetMapping("/api/classes/Paper/resources/", params = ["verified=true"])
    fun loadVerifiedPapers(pageable: Pageable): Page<Resource> {
        return port.loadVerifiedPapers(pageable)
    }

    @GetMapping("/api/classes/Paper/resources/", params = ["verified=false"])
    fun loadUnverifiedPapers(pageable: Pageable): Page<Resource> {
        return port.loadUnverifiedPapers(pageable)
    }
}
