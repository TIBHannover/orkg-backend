package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeCurator
import org.orkg.contenttypes.input.ContentTypeResourcesUseCase
import org.orkg.contenttypes.input.RetrieveAuthorUseCase
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.BaseController
import org.orkg.graph.adapter.input.rest.mapping.AuthorRepresentationAdapter
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.TemplateRepository
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/comparisons", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyComparisonController(
    private val service: ContentTypeResourcesUseCase,
    private val resourceService: ResourceUseCases,
    private val authorService: RetrieveAuthorUseCase,
    override val statementService: StatementUseCases,
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService
) : BaseController(), AuthorRepresentationAdapter {
    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedComparisons(pageable: Pageable) =
        service.loadFeaturedComparisons(pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedComparisons(pageable: Pageable) =
        service.loadNonFeaturedComparisons(pageable)

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorizeCurator
    fun markFeatured(@PathVariable id: ThingId) {
        resourceService.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @PreAuthorizeCurator
    fun unmarkFeatured(@PathVariable id: ThingId) {
        resourceService.markAsNonFeatured(id)
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ThingId): Boolean? =
        resourceService.getFeaturedResourceFlag(id)

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedComparisons(pageable: Pageable) =
        service.loadUnlistedComparisons(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedComparisons(pageable: Pageable) =
        service.loadListedComparisons(pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorizeCurator
    fun markUnlisted(@PathVariable id: ThingId) {
        resourceService.markAsUnlisted(id, ContributorId(authenticatedUserId()))
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @PreAuthorizeCurator
    fun unmarkUnlisted(@PathVariable id: ThingId) {
        resourceService.markAsListed(id)
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ThingId): Boolean = resourceService.getUnlistedResourceFlag(id)

    @GetMapping("/{id}/authors")
    fun getTopAuthors(@PathVariable id: ThingId, pageable: Pageable) =
        authorService.findTopAuthorsOfComparison(id, pageable).mapToComparisonAuthorRepresentation()
}
