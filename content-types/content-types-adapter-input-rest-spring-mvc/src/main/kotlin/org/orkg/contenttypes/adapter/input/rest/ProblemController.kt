package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ContributorId
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.contributorId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.input.RetrieveAuthorUseCase
import org.orkg.contenttypes.input.RetrieveResearchProblemUseCase
import org.orkg.graph.adapter.input.rest.FieldWithFreqRepresentation
import org.orkg.graph.adapter.input.rest.PaperAuthorRepresentation
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.mapping.AuthorRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.FieldPerProblemRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.adapter.input.rest.visibilityFilterFromFlags
import org.orkg.graph.domain.DetailsPerProblem
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
    private val service: RetrieveResearchProblemUseCase,
    private val resourceService: ResourceUseCases,
    private val contributorService: RetrieveContributorUseCase,
    private val authorService: RetrieveAuthorUseCase,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ResourceRepresentationAdapter, AuthorRepresentationAdapter, FieldPerProblemRepresentationAdapter {

    @GetMapping("/{id}/fields")
    fun getFieldPerProblem(
        @PathVariable id: ThingId,
        capabilities: MediaTypeCapabilities
    ): List<FieldWithFreqRepresentation> =
        service.findFieldsPerProblem(id)
            .mapToFieldWithFreqRepresentation(capabilities)

    @GetMapping("/{id}")
    fun getFieldPerProblemAndClasses(
        @PathVariable id: ThingId,
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
            problemId = id,
            classes = classes,
            visibilityFilter = visibility ?: visibilityFilterFromFlags(featured, unlisted),
            pageable = pageable
        )

    @GetMapping("/top")
    fun getTopProblems(capabilities: MediaTypeCapabilities): Iterable<ResourceRepresentation> =
        service.findTopResearchProblems().mapToResourceRepresentation(capabilities)

    @GetMapping("/{id}/users")
    fun getContributorsPerProblem(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): ResponseEntity<Iterable<Any>> {
        val contributors = service.findContributorsPerProblem(id, pageable).map {
            val user = contributorService.findById(ContributorId(it.user)).get()
            object {
                val user = user
                val contributions = it.freq
            }
        }
        return ResponseEntity.ok(contributors)
    }

    @GetMapping("/{id}/authors")
    fun getAuthorsPerProblem(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<PaperAuthorRepresentation> =
        authorService.findAuthorsPerProblem(id, pageable)
            .mapToPaperAuthorRepresentation(capabilities)

    @PutMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.OK)
    fun markFeatured(@PathVariable id: ThingId) {
        resourceService.markAsFeatured(id)
    }

    @DeleteMapping("/{id}/metadata/featured")
    @RequireCuratorRole
    fun unmarkFeatured(@PathVariable id: ThingId) {
        resourceService.markAsNonFeatured(id)
    }

    @PutMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(@PathVariable id: ThingId, currentUser: Authentication?) {
        resourceService.markAsUnlisted(id, currentUser.contributorId())
    }

    @DeleteMapping("/{id}/metadata/unlisted")
    @RequireCuratorRole
    fun unmarkUnlisted(@PathVariable id: ThingId) {
        resourceService.markAsListed(id)
    }
}
