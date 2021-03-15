package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
}
