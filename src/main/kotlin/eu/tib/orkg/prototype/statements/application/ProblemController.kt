package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.rest.UserController.UserDetails
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
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

    @GetMapping("/top")
    fun getTopProblems(): ResponseEntity<Iterable<Resource>> {
        return ResponseEntity.ok(service.getTopResearchProblems())
    }

    @GetMapping("/{problemId}/users")
    fun getContributorsPerProblem(@PathVariable problemId: ResourceId): ResponseEntity<Iterable<Any>> {
        val contributors = service.getContributorsPerProblem(problemId).map {
            val user = userService.findById(it.contributor).get()
            object {
                val user = UserDetails(user)
                val contributions = it.freq
            }
        }
        return ResponseEntity.ok(contributors)
    }
}
