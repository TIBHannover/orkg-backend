package org.orkg.contenttypes.adapter.input.rest

import java.util.*
import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.graph.adapter.input.rest.PaperCountPerResearchProblemRepresentation
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.mapping.PaperCountPerResearchProblemRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * This controller class fetches a list of
 * problems, papers, etc., based on a
 * research field ID
 */
@RestController
@RequestMapping("/api/research-fields", produces = [MediaType.APPLICATION_JSON_VALUE])
class ResearchFieldController(
    private val service: RetrieveResearchFieldUseCase,
    private val resourceService: ResourceUseCases,
    private val comparisonRepository: ComparisonRepository,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ResourceRepresentationAdapter, PaperCountPerResearchProblemRepresentationAdapter {
    /**
     * Fetches all the research problems and
     * number of papers based on a research
     * field {id} that excludes the
     * sub-research fields
     */
    @GetMapping("/{id}/problems")
    fun getResearchProblemsOfField(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<PaperCountPerResearchProblemRepresentation> {
        resourceService.findById(id)
            .orElseThrow { ResourceNotFound.withId(id) }
        return service.getResearchProblemsOfField(id, pageable)
            .mapToPaperCountPerResearchProblemRepresentation(capabilities)
    }

    /**
     * Fetches all the research problems
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/research-problems")
    fun getResearchProblemsIncludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> {
        // Add if condition to check if featured is present and pass the variable
        // Do the same for all
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllResearchProblemsByResearchField(
            id = id,
            visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            includeSubFields = true,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
    }

    /**
     * Fetches all the contributors
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/contributors")
    fun getContributorsIncludingSubFields(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): ResponseEntity<Page<Contributor>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return ok(service.getContributorsIncludingSubFields(id, pageable))
    }

    /**
     * Fetches all the comparisons
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @Deprecated(message = "For removal", replaceWith = ReplaceWith("/api/comparisons?research_field={id}"))
    @GetMapping("/{id}/subfields/comparisons")
    fun getComparisonsIncludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return comparisonRepository.findAll(
            researchField = id,
            visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            includeSubfields = true,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
    }

    /**
     * Fetches all the papers
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/papers")
    fun getPapersIncludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllPapersByResearchField(
            id = id,
            visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            includeSubFields = true,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
    }

    /**
     * Fetches all the papers
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/papers")
    fun getPapersExcludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllPapersByResearchField(
            id = id,
            visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            includeSubFields = false,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
    }

    /**
     * Fetches all the comparisons
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @Deprecated(message = "For removal", replaceWith = ReplaceWith("/api/comparisons?research_field={id}"))
    @GetMapping("/{id}/comparisons")
    fun getComparisonsExcludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return comparisonRepository.findAll(
            researchField = id,
            visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            includeSubfields = false,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
    }

    /**
     * Fetches all the contributors
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/contributors")
    fun getContributorsExcludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured")
        featured: Optional<Boolean>,
        pageable: Pageable
    ): ResponseEntity<Page<Contributor>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return ok(service.getContributorsExcludingSubFields(id, pageable))
    }

    /**
     * Fetches all the research problems
     * based on a research field {id}
     */
    @GetMapping("/{id}/research-problems")
    fun getResearchProblemsExcludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllResearchProblemsByResearchField(
            id = id,
            visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            includeSubFields = false,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
    }

    /**
     * Gets entities based on the provided classes including subfields
     *
     */
    @GetMapping("/{id}/subfields")
    fun getEntitiesBasedOnClassesIncludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        @RequestParam("classes")
        classes: List<String>,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        service.findAllEntitiesBasedOnClassesByResearchField(
            id = id,
            classesList = classes,
            visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            includeSubFields = true,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)

    /**
     * Gets entities based on the provided classes excluding subfields
     *
     */
    @GetMapping("/{id}")
    fun getEntitiesBasedOnClassesExcludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("visibility", required = false)
        visibility: VisibilityFilter?,
        @RequestParam("classes")
        classes: List<String>,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities
    ): Page<ResourceRepresentation> =
        service.findAllEntitiesBasedOnClassesByResearchField(
            id = id,
            classesList = classes,
            visibility = visibility ?: VisibilityFilter.fromFlags(featured, unlisted),
            includeSubFields = false,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
}
