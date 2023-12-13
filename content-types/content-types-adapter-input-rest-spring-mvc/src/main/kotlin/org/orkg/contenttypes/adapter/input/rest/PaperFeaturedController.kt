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
class PaperFeaturedController(
    private val port: LoadPaperPort,
    private val query: GetPaperFlagQuery
) {

    @GetMapping("/api/papers/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ThingId): Boolean? = query.getFeaturedPaperFlag(id)

    @GetMapping("/api/classes/Paper/featured/resources/", params = ["featured=true"])
    fun loadFeaturedPapers(pageable: Pageable): Page<Resource> {
        return port.loadFeaturedPapers(pageable)
    }

    @GetMapping("/api/classes/Paper/featured/resources/", params = ["featured=false"])
    fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource> {
        return port.loadNonFeaturedPapers(pageable)
    }
}
