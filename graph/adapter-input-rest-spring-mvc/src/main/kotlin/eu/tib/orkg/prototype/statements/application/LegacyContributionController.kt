package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contributions", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyContributionController(
    private val neo4jResourceService: ResourceUseCases,
    private val service: ResourceUseCases
) {
    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedContributions(pageable: Pageable) =
        neo4jResourceService.loadFeaturedContributions(pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedContributions(pageable: Pageable) =
        neo4jResourceService.loadNonFeaturedContributions(pageable)

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun markFeatured(@PathVariable id: ThingId) {
        service.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun unmarkFeatured(@PathVariable id: ThingId) {
        service.markAsNonFeatured(id)
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ThingId): Boolean = service.getFeaturedResourceFlag(id)

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedContributions(pageable: Pageable) =
        neo4jResourceService.loadUnlistedContributions(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedContributions(pageable: Pageable) =
        neo4jResourceService.loadListedContributions(pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(@PathVariable id: ThingId) {
        neo4jResourceService.markAsUnlisted(id)
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun unmarkUnlisted(@PathVariable id: ThingId) {
        neo4jResourceService.markAsListed(id)
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ThingId): Boolean = service.getUnlistedResourceFlag(id)
}
