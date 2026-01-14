package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.adapter.input.rest.mapping.ContributorWithContributionCountRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.FieldPerProblemRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.LegacyAuthorRepresentationAdapter
import org.orkg.contenttypes.input.AuthorUseCases
import org.orkg.contenttypes.input.LegacyResearchProblemUseCases
import org.orkg.graph.adapter.input.rest.FieldWithFreqRepresentation
import org.orkg.graph.adapter.input.rest.PaperAuthorRepresentation
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
@RequestMapping("/api/problems", produces = [MediaType.APPLICATION_JSON_VALUE])
class LegacyProblemController(
    private val service: LegacyResearchProblemUseCases,
    override val contributorService: RetrieveContributorUseCase,
    private val authorService: AuthorUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : LegacyAuthorRepresentationAdapter,
    FieldPerProblemRepresentationAdapter,
    ContributorWithContributionCountRepresentationAdapter {
    @GetMapping("/{id}/fields")
    fun findAllResearchFields(
        @PathVariable id: ThingId,
        capabilities: MediaTypeCapabilities,
    ): List<FieldWithFreqRepresentation> =
        service.findAllResearchFields(id)
            .mapToFieldWithFreqRepresentation(capabilities)

    @GetMapping("/{id}/users")
    fun findAllContributorsPerProblem(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): List<ContributorWithContributionCountRepresentation> =
        service.findAllContributorsPerProblem(id, pageable)
            .mapToContributorWithContributionCountRepresentation()

    @GetMapping("/{id}/authors")
    fun findAllByProblemId(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<PaperAuthorRepresentation> =
        authorService.findAllByProblemId(id, pageable)
            .mapToPaperAuthorRepresentation(capabilities)
}
