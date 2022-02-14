package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.ResourceService
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
@RequestMapping("/api/comparisons")
class ComparisonController(
    private val neo4jResourceService: ResourceService,
    private val service: ResourceUseCases
) {
    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedComparisons(pageable: Pageable) =
        neo4jResourceService.loadFeaturedComparisons(pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedComparisons(pageable: Pageable) =
        neo4jResourceService.loadNonFeaturedComparisons(pageable)

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
    fun getFeaturedFlag(@PathVariable id: ResourceId): Boolean? =
        service.getFeaturedResourceFlag(id)

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedComparisons(pageable: Pageable) =
        neo4jResourceService.loadUnlistedComparisons(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedComparisons(pageable: Pageable) =
        neo4jResourceService.loadListedComparisons(pageable)

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
