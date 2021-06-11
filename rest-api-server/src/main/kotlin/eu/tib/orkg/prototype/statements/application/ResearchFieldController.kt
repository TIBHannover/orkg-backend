package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * This controller class fetches a list of
 * problems, papers, etc., based on a
 * research field ID
 */
@RestController
@RequestMapping("/api/research-fields/")
class ResearchFieldController(
    private val service: ResearchFieldService,
    private val resourceService: ResourceService
) {

    /**
     * Fetches all the research problems and
     * number of papers based on a research
     * field {id} that excludes the
     * sub-research fields
     */
    @GetMapping("/{id}/problems")
    fun getResearchProblemsOfField(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Any>> {
        resourceService.findById(id)
            .orElseThrow { ResourceNotFound() }
        return ok(service.getResearchProblemsOfField(id, pageable))
    }

    /**
     * Fetches all the research problems
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/research-problems")
    fun getResearchProblemsIncludingSubFields(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getResearchProblemsIncludingSubFields(id, pageable))
    }

    /**
     * Fetches all the contributors
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/contributors")
    fun getContributorsIncludingSubFields(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Contributor>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getContributorsIncludingSubFields(id, pageable))
    }

    /**
     * Fetches all the comparisons
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/comparisons")
    fun getComparisonsIncludingSubFields(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getComparisonsIncludingSubFields(id, pageable))
    }

    /**
     * Fetches all the papers
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/papers")
    fun getPapersIncludingSubFields(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getPapersIncludingSubFields(id, pageable))
    }

    /**
     * Fetches all the papers
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/papers")
    fun getPapersExcludingSubFields(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getPapersExcludingSubFields(id, pageable))
    }

    /**
     * Fetches all the comparisons
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/comparisons")
    fun getComparisonsExcludingSubFields(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getComparisonsExcludingSubFields(id, pageable))
    }

    /**
     * Fetches all the contributors
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/contributors")
    fun getContributorsExcludingSubFields(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Contributor>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getContributorsExcludingSubFields(id, pageable))
    }

    /**
     * Fetches all the research problems
     * based on a research field {id}
     */
    @GetMapping("/{id}/research-problems")
    fun getResearchProblemsExcludingSubFields(
        @PathVariable id: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getResearchProblemsExcludingSubFields(id, pageable))
    }
}
