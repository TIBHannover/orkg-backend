package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.MediaTypeCapabilities
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.contenttypes.adapter.input.rest.mapping.PaperCountPerResearchProblemRepresentationAdapter
import org.orkg.contenttypes.input.ResearchFieldUseCases
import org.orkg.graph.adapter.input.rest.PaperCountPerResearchProblemRepresentation
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
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
import java.util.Optional

/**
 * This controller class fetches a list of
 * problems, papers, etc., based on a
 * research field ID
 */
@RestController
@RequestMapping("/api/research-fields", produces = [MediaType.APPLICATION_JSON_VALUE])
class ResearchFieldController(
    private val service: ResearchFieldUseCases,
    private val resourceService: ResourceUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelService: FormattedLabelUseCases,
) : ResourceRepresentationAdapter,
    PaperCountPerResearchProblemRepresentationAdapter {
    /**
     * Fetches all the research problems and
     * number of papers based on a research
     * field {id} that excludes the
     * sub-research fields
     */
    @GetMapping("/{id}/problems")
    fun findAllPaperCountsPerResearchProblem(
        @PathVariable id: ThingId,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<PaperCountPerResearchProblemRepresentation> {
        resourceService.findById(id)
            .orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllPaperCountsPerResearchProblem(id, pageable)
            .mapToPaperCountPerResearchProblemRepresentation(capabilities)
    }

    /**
     * Fetches all the research problems
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/research-problems")
    fun findAllResearchProblemsByResearchFieldIncludingSubfields(
        @PathVariable id: ThingId,
        @RequestParam("visibility") visibility: VisibilityFilter = VisibilityFilter.ALL_LISTED,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<ResourceRepresentation> {
        // Add if condition to check if featured is present and pass the variable
        // Do the same for all
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllResearchProblemsByResearchField(
            id = id,
            visibility = visibility,
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
    fun findAllContributorsIncludingSubFields(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): ResponseEntity<Page<Contributor>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return ok(service.findAllContributorsIncludingSubFields(id, pageable))
    }

    /**
     * Fetches all the contributors
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/contributors")
    fun findAllContributorsExcludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured")
        featured: Optional<Boolean>,
        pageable: Pageable,
    ): ResponseEntity<Page<Contributor>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return ok(service.findAllContributorsExcludingSubFields(id, pageable))
    }

    /**
     * Fetches all the research problems
     * based on a research field {id}
     */
    @GetMapping("/{id}/research-problems")
    fun findAllResearchProblemsByResearchFieldExcludingSubfields(
        @PathVariable id: ThingId,
        @RequestParam("visibility") visibility: VisibilityFilter = VisibilityFilter.ALL_LISTED,
        pageable: Pageable,
        capabilities: MediaTypeCapabilities,
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllResearchProblemsByResearchField(
            id = id,
            visibility = visibility,
            includeSubFields = false,
            pageable = pageable
        ).mapToResourceRepresentation(capabilities)
    }
}
