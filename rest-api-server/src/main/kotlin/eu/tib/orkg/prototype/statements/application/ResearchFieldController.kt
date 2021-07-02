package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        // Add if condition to check if featured is present and pass the variable
        // Do the same for all
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getResearchProblemsIncludingSubFields(
            id = id,
            featured = featured,
            unlisted = unlisted,
            pageable = pageable))
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
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getComparisonsIncludingSubFields(
            id = id,
            featured = featured,
            unlisted = unlisted,
            pageable = pageable
        ))
    }

    /**
     * Fetches all the papers
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/papers")
    fun getPapersIncludingSubFields(
        @PathVariable id: ResourceId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getPapersIncludingSubFields(
            id = id,
            featured = featured,
            unlisted = unlisted,
            pageable = pageable))
    }

    /**
     * Fetches all the papers
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/papers")
    fun getPapersExcludingSubFields(
        @PathVariable id: ResourceId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getPapersExcludingSubFields(id = id,
            featured = featured,
            unlisted = unlisted,
            pageable = pageable))
    }

    /**
     * Fetches all the comparisons
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/comparisons")
    fun getComparisonsExcludingSubFields(
        @PathVariable id: ResourceId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getComparisonsExcludingSubFields(
            id = id,
            featured = featured,
            unlisted = unlisted,
            pageable = pageable))
    }

    /**
     * Fetches all the contributors
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/contributors")
    fun getContributorsExcludingSubFields(
        @PathVariable id: ResourceId,
        @RequestParam("featured")
        featured: Optional<Boolean>,
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
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>> {
        resourceService.findById(id).orElseThrow { ResourceNotFound() }
        return ok(service.getResearchProblemsExcludingSubFields(
            id = id,
            featured = featured,
            unlisted = unlisted,
            pageable = pageable))
    }

    @GetMapping("/{id}/subfields/")
    fun getEntitiesBasedOnClassesIncludingSubFields(
        @PathVariable id: ResourceId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("classes")
        classes: List<String>,
        pageable: Pageable
    ): ResponseEntity<Page<Resource>>{
        return ok(service.getEntitiesBasedOnClassesIncludingSubfields(id, classes, featured, pageable))
    }
}
