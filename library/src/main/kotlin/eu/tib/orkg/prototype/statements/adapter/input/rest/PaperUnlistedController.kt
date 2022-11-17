package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.application.port.out.GetPaperFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperPort
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PaperUnlistedController(
    @Qualifier("resourceService")
    private val port: LoadPaperPort,
    @Qualifier("resourceService")
    private val query: GetPaperFlagQuery
) : BaseController() {

    @GetMapping("/api/papers/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ResourceId): Boolean = query.getUnlistedPaperFlag(id)

    @GetMapping("/api/classes/Paper/unlisted/resources/", params = ["unlisted=true"])
    fun loadUnlistedPapers(pageable: Pageable): Page<Resource> {
        return port.loadUnlistedPapers(pageable)
    }

    @GetMapping("/api/classes/Paper/unlisted/resources/", params = ["unlisted=false"])
    fun loadListedPapers(pageable: Pageable): Page<Resource> {
        return port.loadListedPapers(pageable)
    }
}
