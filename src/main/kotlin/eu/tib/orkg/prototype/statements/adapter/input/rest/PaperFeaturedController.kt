package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.application.port.out.GetPaperFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperAdapter
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PaperFeaturedController(
    private val adapter: LoadPaperAdapter,
    private val query: GetPaperFlagQuery
) : BaseController() {

    @GetMapping("/api/papers/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ResourceId): Boolean =
        query.getFeaturedPaperFlag(id) ?: throw ResourceNotFound(id.toString())

    @GetMapping("/api/classes/Paper/featured/resources/", params = ["featured=true"])
    fun loadFeaturedPapers(pageable: Pageable): Page<Resource> {
        return adapter.loadFeaturedPapers(pageable)
    }

    @GetMapping("/api/classes/Paper/featured/resources/", params = ["featured=false"])
    fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource> {
        return adapter.loadNonFeaturedPapers(pageable)
    }
}