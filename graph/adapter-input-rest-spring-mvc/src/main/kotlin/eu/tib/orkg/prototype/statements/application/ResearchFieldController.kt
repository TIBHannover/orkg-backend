package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase.PaperCountPerResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.lang.Nullable
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
    private val service: RetrieveResearchFieldUseCase,
    private val resourceService: ResourceUseCases
) {
    /**
     * Fetches all the research problems and
     * number of papers based on a research
     * field {id} that excludes the
     * sub-research fields
     */
    @GetMapping("/{id}/problems")
    fun getResearchProblemsOfField(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): ResponseEntity<Page<PaperCountPerResearchProblem>> {
        resourceService.findById(id)
            .orElseThrow { ResourceNotFound.withId(id) }
        return ok(service.getResearchProblemsOfField(id, pageable))
    }

    /**
     * Fetches all the research problems
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/research-problems")
    fun getResearchProblemsIncludingSubFields(
        @PathVariable id: ThingId,
        @Nullable @RequestParam("featured")
        featured: Boolean?,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        // Add if condition to check if featured is present and pass the variable
        // Do the same for all
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllResearchProblemsByResearchField(
            id = id,
            visibility = visibilityFilterFromFlags(featured, unlisted),
            includeSubFields = true,
            pageable = pageable
        )
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
    @GetMapping("/{id}/subfields/comparisons")
    fun getComparisonsIncludingSubFields(
        @PathVariable id: ThingId,
        @Nullable @RequestParam("featured")
        featured: Boolean?,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllComparisonsByResearchField(
            id = id,
            visibility = visibilityFilterFromFlags(featured, unlisted),
            includeSubFields = true,
            pageable = pageable
        )
    }

    /**
     * Fetches all the papers
     * based on a research field {id}
     * that includes the sub-research fields
     */
    @GetMapping("/{id}/subfields/papers")
    fun getPapersIncludingSubFields(
        @PathVariable id: ThingId,
        @Nullable @RequestParam("featured")
        featured: Boolean?,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllPapersByResearchField(
            id = id,
            visibility = visibilityFilterFromFlags(featured, unlisted),
            includeSubFields = true,
            pageable = pageable
        )
    }

    /**
     * Fetches all the papers
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/papers")
    fun getPapersExcludingSubFields(
        @PathVariable id: ThingId,
        @Nullable @RequestParam("featured")
        featured: Boolean?,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllPapersByResearchField(
            id = id,
            visibility = visibilityFilterFromFlags(featured, unlisted),
            includeSubFields = false,
            pageable = pageable
        )
    }

    /**
     * Fetches all the comparisons
     * based on a research field {id}
     * that excludes the sub-research fields
     */
    @GetMapping("/{id}/comparisons")
    fun getComparisonsExcludingSubFields(
        @PathVariable id: ThingId,
        @Nullable @RequestParam("featured")
        featured: Boolean?,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllComparisonsByResearchField(
            id = id,
            visibility = visibilityFilterFromFlags(featured, unlisted),
            includeSubFields = false,
            pageable = pageable
        )
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
        @Nullable @RequestParam("featured")
        featured: Boolean?,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        resourceService.findById(id).orElseThrow { ResourceNotFound.withId(id) }
        return service.findAllResearchProblemsByResearchField(
            id = id,
            visibility = visibilityFilterFromFlags(featured, unlisted),
            includeSubFields = false,
            pageable = pageable
        )
    }

    /**
     * Gets entities based on the provided classes including sub fields
     *
     */
    @GetMapping("/{id}/subfields/")
    fun getEntitiesBasedOnClassesIncludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        @Nullable
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("classes")
        classes: List<String>,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        service.findAllEntitiesBasedOnClassesByResearchField(
            id = id,
            classesList = classes,
            visibility = visibilityFilterFromFlags(featured, unlisted),
            includeSubFields = true,
            pageable = pageable
        )

    /**
     * Gets entities based on the provided classes excluding sub fields
     *
     */
    @GetMapping("/{id}")
    fun getEntitiesBasedOnClassesExcludingSubFields(
        @PathVariable id: ThingId,
        @RequestParam("featured", required = false, defaultValue = "false")
        featured: Boolean,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        @RequestParam("classes")
        classes: List<String>,
        pageable: Pageable
    ): Page<ResourceRepresentation> =
        service.findAllEntitiesBasedOnClassesByResearchField(
            id = id,
            classesList = classes,
            visibility = visibilityFilterFromFlags(featured, unlisted),
            includeSubFields = false,
            pageable = pageable
        )
}
