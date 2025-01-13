package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.contributorId
import org.orkg.contenttypes.input.RetrieveAuthorUseCase
import org.orkg.graph.adapter.input.rest.ComparisonAuthorRepresentation
import org.orkg.graph.adapter.input.rest.mapping.AuthorRepresentationAdapter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
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
    private val resourceService: ResourceUseCases,
    private val authorService: RetrieveAuthorUseCase,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : AuthorRepresentationAdapter {
    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    @RequireCuratorRole
    fun markFeatured(@PathVariable id: ThingId) {
        resourceService.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    fun unmarkFeatured(@PathVariable id: ThingId) {
        resourceService.markAsNonFeatured(id)
    }

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    @RequireCuratorRole
    fun markUnlisted(@PathVariable id: ThingId, currentUser: Authentication?) {
        resourceService.markAsUnlisted(id, currentUser.contributorId())
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    fun unmarkUnlisted(@PathVariable id: ThingId) {
        resourceService.markAsListed(id)
    }

    @GetMapping("/{id}/authors")
    fun getTopAuthors(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ComparisonAuthorRepresentation> =
        authorService.findTopAuthorsOfComparison(id, pageable)
            .mapToComparisonAuthorRepresentation(capabilities)
}
