package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.LegacyAuthorRepresentationAdapter
import org.orkg.contenttypes.input.AuthorUseCases
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/comparisons", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyComparisonController(
    private val authorService: AuthorUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : LegacyAuthorRepresentationAdapter {
    @GetMapping("/{id}/authors")
    fun getTopAuthors(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<ComparisonAuthorRepresentation> =
        authorService.findTopAuthorsOfComparison(id, pageable)
            .mapToComparisonAuthorRepresentation(capabilities)
}
