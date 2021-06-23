package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jResourceService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/visualizations")
class VisualizationController(
    private val neo4jResourceService: Neo4jResourceService,
    private val service: ResourceService
) {
    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedVisualizations(pageable: Pageable) =
        neo4jResourceService.loadFeaturedVisualizations(pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedVisualizations(pageable: Pageable) =
        neo4jResourceService.loadNonFeaturedVisualizations(pageable)

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    fun markFeatured(@PathVariable id: ResourceId) {
        service.markAsFeatured(id).orElseThrow { ResourceNotFound(id.toString()) }
    }
    @DeleteMapping("/{id}/metadata/featured")
    fun unmarkFeatured(@PathVariable id: ResourceId) {
        service.markAsNonFeatured(id).orElseThrow { ResourceNotFound(id.toString()) }
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ResourceId): Boolean =
        service.getFeaturedResourceFlag(id) ?: throw ResourceNotFound(id.toString())

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedVisualizations(pageable: Pageable) =
        neo4jResourceService.loadUnlistedVisualizations(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedVisualizations(pageable: Pageable) =
        neo4jResourceService.loadListedVisualizations(pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(@PathVariable id: ResourceId) {
        neo4jResourceService.markAsUnlisted(id).orElseThrow { ResourceNotFound(id.toString()) }
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    fun unmarkUnlisted(@PathVariable id: ResourceId) {
        neo4jResourceService.markAsListed(id).orElseThrow { ResourceNotFound(id.toString()) }
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ResourceId): Boolean =
        service.getUnlistedResourceFlag(id) ?: throw ResourceNotFound(id.toString())
}
