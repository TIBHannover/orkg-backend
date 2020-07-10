package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/problems/")
class ProblemController(private val service: ProblemService) {

    @GetMapping("/{problemId}/fields")
    fun getFieldPerProblem(@PathVariable problemId: ResourceId): ResponseEntity<Iterable<Any>> {
        return ResponseEntity.ok(service.getFieldsPerProblem(problemId))
    }
}
