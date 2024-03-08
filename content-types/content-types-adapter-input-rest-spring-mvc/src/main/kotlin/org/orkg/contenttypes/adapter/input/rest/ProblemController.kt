package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeCurator
import org.orkg.common.contributorId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.input.RetrieveAuthorUseCase
import org.orkg.contenttypes.input.RetrieveResearchProblemUseCase
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.AuthorRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.FieldPerProblemRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.adapter.input.rest.visibilityFilterFromFlags
import org.orkg.graph.domain.DetailsPerProblem
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.adapter.input.rest.FieldWithFreqRepresentation
import org.orkg.graph.adapter.input.rest.PaperAuthorRepresentation
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/problems/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ProblemController(
    private val service: RetrieveResearchProblemUseCase, // FIXME
    private val resourceService: ResourceUseCases,
    private val contributorService: RetrieveContributorUseCase,
    private val authorService: RetrieveAuthorUseCase,
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService,
) : ResourceRepresentationAdapter, AuthorRepresentationAdapter, FieldPerProblemRepresentationAdapter {

    @GetMapping("/{problemId}/fields")
    fun getFieldPerProblem(@PathVariable problemId: ThingId): Iterable<FieldWithFreqRepresentation> {
        return service.findFieldsPerProblem(problemId).mapToFieldWithFreqRepresentation()
    }

    @GetMapping("/{problemId}/")
    fun getFieldPerProblemAndClasses(
        @PathVariable problemId: ThingId,
        @RequestParam(value = "classes") classes: List<String>,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable
    ): Page<DetailsPerProblem> =
        service.findAllEntitiesBasedOnClassByProblem(
            problemId = problemId,
            classes = classes,
            visibilityFilter = visibility ?: visibilityFilterFromFlags(featured, unlisted),
            pageable = pageable
        )

    @GetMapping("/top")
    fun getTopProblems(): Iterable<ResourceRepresentation> =
        service.findTopResearchProblems().mapToResourceRepresentation()

    @GetMapping("/{problemId}/users")
    fun getContributorsPerProblem(
        @PathVariable problemId: ThingId,
        pageable: Pageable
    ): ResponseEntity<Iterable<Any>> {
        val contributors = service.findContributorsPerProblem(problemId, pageable).map {
            val user = contributorService.findById(ContributorId(it.user)).get()
            object {
                val user = user
                val contributions = it.freq
            }
        }
        return ResponseEntity.ok(contributors)
    }

    @GetMapping("/{problemId}/authors")
    fun getAuthorsPerProblem(
        @PathVariable problemId: ThingId,
        pageable: Pageable
    ): Page<PaperAuthorRepresentation> =
        authorService.findAuthorsPerProblem(problemId, pageable).mapToPaperAuthorRepresentation()

    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedProblems(pageable: Pageable) =
        service.loadFeaturedProblems(pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedProblems(pageable: Pageable) =
        service.loadNonFeaturedProblems(pageable)

    @PutMapping("/{id}/metadata/featured")
    @PreAuthorizeCurator
    @ResponseStatus(HttpStatus.OK)
    fun markFeatured(@PathVariable id: ThingId) {
        resourceService.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @PreAuthorizeCurator
    fun unmarkFeatured(@PathVariable id: ThingId) {
        resourceService.markAsNonFeatured(id)
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ThingId): Boolean = service.getFeaturedProblemFlag(id)

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedContributions(pageable: Pageable) =
        service.loadUnlistedProblems(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedContributions(pageable: Pageable) =
        service.loadListedProblems(pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @PreAuthorizeCurator
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(@PathVariable id: ThingId, @AuthenticationPrincipal currentUser: UserDetails?) {
        resourceService.markAsUnlisted(id, currentUser.contributorId())
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @PreAuthorizeCurator
    fun unmarkUnlisted(@PathVariable id: ThingId) {
        resourceService.markAsListed(id)
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ThingId): Boolean? = service.getUnlistedProblemFlag(id)
}
