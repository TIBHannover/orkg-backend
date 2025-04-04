package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.contributorId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.adapter.input.rest.mapping.ContributorWithContributionCountRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.FieldPerProblemRepresentationAdapter
import org.orkg.contenttypes.adapter.input.rest.mapping.LegacyAuthorRepresentationAdapter
import org.orkg.contenttypes.input.AuthorUseCases
import org.orkg.contenttypes.input.ResearchProblemUseCases
import org.orkg.graph.adapter.input.rest.FieldWithFreqRepresentation
import org.orkg.graph.adapter.input.rest.PaperAuthorRepresentation
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.DetailsPerProblem
import org.orkg.graph.domain.VisibilityFilter
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/problems", produces = [MediaType.APPLICATION_JSON_VALUE])
class ProblemController(
    private val service: ResearchProblemUseCases,
    private val resourceService: ResourceUseCases,
    override val contributorService: RetrieveContributorUseCase,
    private val authorService: AuthorUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ResourceRepresentationAdapter,
    LegacyAuthorRepresentationAdapter,
    FieldPerProblemRepresentationAdapter,
    ContributorWithContributionCountRepresentationAdapter {
    @GetMapping("/{id}/fields")
    fun findAllResearchFields(
        @PathVariable id: ThingId,
        capabilities: MediaTypeCapabilities,
    ): List<FieldWithFreqRepresentation> =
        service.findAllResearchFields(id)
            .mapToFieldWithFreqRepresentation(capabilities)

    @GetMapping("/{id}")
    fun findAllEntitiesBasedOnClassByProblem(
        @PathVariable id: ThingId,
        @RequestParam(value = "classes") classes: List<String>,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
    ): Page<DetailsPerProblem> =
        service.findAllEntitiesBasedOnClassByProblem(
            problemId = id,
            classes = classes,
            visibilityFilter = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            pageable = pageable
        )

    @GetMapping("/top")
    fun findTopResearchProblems(capabilities: MediaTypeCapabilities): Iterable<ResourceRepresentation> =
        service.findTopResearchProblems().mapToResourceRepresentation(capabilities)

    @GetMapping("/{id}/users")
    fun findAllContributorsPerProblem(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): List<ContributorWithContributionCountRepresentation> =
        service.findAllContributorsPerProblem(id, pageable).mapToContributorWithContributionCountRepresentation()

    @GetMapping("/{id}/authors")
    fun findAllByProblemId(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<PaperAuthorRepresentation> =
        authorService.findAllByProblemId(id, pageable)
            .mapToPaperAuthorRepresentation(capabilities)

    @PutMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.OK)
    fun markFeatured(
        @PathVariable id: ThingId,
    ) {
        resourceService.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    fun unmarkFeatured(
        @PathVariable id: ThingId,
    ) {
        resourceService.markAsNonFeatured(id)
    }

    @PutMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(
        @PathVariable id: ThingId,
        currentUser: Authentication?,
    ) {
        resourceService.markAsUnlisted(id, currentUser.contributorId())
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    fun unmarkUnlisted(
        @PathVariable id: ThingId,
    ) {
        resourceService.markAsListed(id)
    }
}
