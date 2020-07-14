package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.rest.UserController.UserDetails
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
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
    private val userService: UserService
) {

    @GetMapping("/{problemId}/fields")
    fun getFieldPerProblem(@PathVariable problemId: ResourceId): ResponseEntity<Iterable<Any>> {
        return ResponseEntity.ok(service.getFieldsPerProblem(problemId))
    }

    @GetMapping("/{problemId}/users")
    fun getContributorsPerProblem(
        @PathVariable problemId: ResourceId,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?
    ): ResponseEntity<Iterable<Any>> {
        val pagination = createPageable(page, items, null, false)
        val contributors = service.getContributorsPerProblem(problemId, pagination).map {
            val user = userService.findById(it.contributor).get()
            object {
                val user = UserDetails(user)
                val contributions = it.freq
            }
        }
        return ResponseEntity.ok(contributors)
    }

    @GetMapping("/{problemId}/authors")
    fun getAuthorsPerProblem(
        @PathVariable problemId: ResourceId,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?
    ): ResponseEntity<Iterable<Any>> {
        val pagination = createPageable(page, items, null, false)
        return ResponseEntity.ok(service.getAuthorsPerProblem(problemId, pagination))
    }
}
