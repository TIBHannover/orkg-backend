package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import eu.tib.orkg.prototype.statements.services.AuthorService
import org.springframework.http.MediaType

@RestController
@RequestMapping("/api/comparisons", produces = [MediaType.APPLICATION_JSON_VALUE])
class ComparisonController(
    private val service: ResourceUseCases,
    private val authorService: AuthorService
) {
    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedComparisons(pageable: Pageable) =
        service.loadFeaturedComparisons(pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedComparisons(pageable: Pageable) =
        service.loadNonFeaturedComparisons(pageable)

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    fun markFeatured(@PathVariable id: ThingId) {
        service.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    fun unmarkFeatured(@PathVariable id: ThingId) {
        service.markAsNonFeatured(id)
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ThingId): Boolean? =
        service.getFeaturedResourceFlag(id)

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedComparisons(pageable: Pageable) =
        service.loadUnlistedComparisons(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedComparisons(pageable: Pageable) =
        service.loadListedComparisons(pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(@PathVariable id: ThingId) {
        service.markAsUnlisted(id)
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    fun unmarkUnlisted(@PathVariable id: ThingId) {
        service.markAsListed(id)
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ThingId): Boolean = service.getUnlistedResourceFlag(id)

    @GetMapping("/{id}/authors")
    fun getTopAuthors(@PathVariable id: ThingId, pageable: Pageable) =
        authorService.findTopAuthorsOfComparison(id, pageable)
}
