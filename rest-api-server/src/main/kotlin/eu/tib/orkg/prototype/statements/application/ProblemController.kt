package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.DetailsPerProblem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.lang.Nullable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/problems/")
class ProblemController(
    private val service: ProblemService,
    private val contributorService: ContributorService
) {

    @GetMapping("/{problemId}/fields")
    fun getFieldPerProblem(@PathVariable problemId: ResourceId): ResponseEntity<Iterable<Any>> {
        return ResponseEntity.ok(service.findFieldsPerProblem(problemId))
    }

    @GetMapping("/{problemId}/")
    fun getFieldPerProblemAndClasses(
        @PathVariable problemId: ResourceId,
        @RequestParam(value = "classes") classes: List<String>,
        @Nullable @RequestParam("featured")
        featured: Boolean?,
        @RequestParam("unlisted", required = false, defaultValue = "false")
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem> {
        return service.findFieldsPerProblemAndClasses(problemId, featured, unlisted, classes, pageable)
    }

    @GetMapping("/top")
    fun getTopProblems(): ResponseEntity<Iterable<Resource>> {
        return ResponseEntity.ok(service.findTopResearchProblems())
    }

    @GetMapping("/{problemId}/users")
    fun getContributorsPerProblem(
        @PathVariable problemId: ResourceId,
        pageable: Pageable
    ): ResponseEntity<Iterable<Any>> {
        val contributors = service.findContributorsPerProblem(problemId, pageable).map {
            val user = contributorService.findById(ContributorId(it.contributor)).get()
            object {
                val user = user
                val contributions = it.freq
            }
        }
        return ResponseEntity.ok(contributors)
    }

    @GetMapping("/{problemId}/authors")
    fun getAuthorsPerProblem(
        @PathVariable problemId: ResourceId,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        pageable: Pageable
    ): ResponseEntity<Iterable<Any>> {
        return ResponseEntity.ok(service.findAuthorsPerProblem(problemId, pageable))
    }

    @GetMapping("/metadata/featured", params = ["featured=true"])
    fun getFeaturedProblems(pageable: Pageable) =
        service.loadFeaturedProblems(pageable)

    @GetMapping("/metadata/featured", params = ["featured=false"])
    fun getNonFeaturedProblems(pageable: Pageable) =
        service.loadNonFeaturedProblems(pageable)

    @PutMapping("/{id}/metadata/featured")
    @ResponseStatus(HttpStatus.OK)
    fun markFeatured(@PathVariable id: ResourceId) {
        service.markAsFeatured(id).orElseThrow { ResourceNotFound(id.toString()) }
    }
    @DeleteMapping("/{id}/metadata/featured")
    fun unmarkFeatured(@PathVariable id: ResourceId) {
        service.markAsNonFeatured(id).orElseThrow { ResourceNotFound(id.toString()) }
    }

    @GetMapping("/{id}/metadata/featured")
    fun getFeaturedFlag(@PathVariable id: ResourceId): Boolean =
        service.getFeaturedProblemFlag(id) ?: throw ResourceNotFound(id.toString())

    @GetMapping("/metadata/unlisted", params = ["unlisted=true"])
    fun getUnlistedContributions(pageable: Pageable) =
        service.loadUnlistedProblems(pageable)

    @GetMapping("/metadata/unlisted", params = ["unlisted=false"])
    fun getListedContributions(pageable: Pageable) =
        service.loadListedProblems(pageable)

    @PutMapping("/{id}/metadata/unlisted")
    @ResponseStatus(HttpStatus.OK)
    fun markUnlisted(@PathVariable id: ResourceId) {
        service.markAsUnlisted(id).orElseThrow { ResourceNotFound(id.toString()) }
    }
    @DeleteMapping("/{id}/metadata/unlisted")
    fun unmarkUnlisted(@PathVariable id: ResourceId) {
        service.markAsListed(id).orElseThrow { ResourceNotFound(id.toString()) }
    }

    @GetMapping("/{id}/metadata/unlisted")
    fun getUnlistedFlag(@PathVariable id: ResourceId): Boolean =
        service.getUnlistedProblemFlag(id) ?: throw ResourceNotFound(id.toString())
}
