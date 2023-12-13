package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.GetPaperFlagQuery
import org.orkg.contenttypes.input.LoadPaperPort
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PaperUnlistedController(
    private val port: LoadPaperPort,
    private val query: GetPaperFlagQuery
) {

    @GetMapping("/api/papers/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ThingId): Boolean = query.getUnlistedPaperFlag(id)

    @GetMapping("/api/classes/Paper/unlisted/resources/", params = ["unlisted=true"])
    fun loadUnlistedPapers(pageable: Pageable): Page<Resource> {
        return port.loadUnlistedPapers(pageable)
    }

    @GetMapping("/api/classes/Paper/unlisted/resources/", params = ["unlisted=false"])
    fun loadListedPapers(pageable: Pageable): Page<Resource> {
        return port.loadListedPapers(pageable)
    }
}
