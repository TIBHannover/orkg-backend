package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.domain.model.StatementRepository
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements")
@CrossOrigin(origins = ["*"])
class StatementController(private val repository: StatementRepository) {

    @GetMapping("/")
    fun findAll(): Iterable<Statement> {
        return repository.findAll()
    }

    @GetMapping("/subject/{resourceId}")
    fun findByResource(@PathVariable resourceId: ResourceId) =
        repository.findBySubject(resourceId)

    @PostMapping("/")
    fun add(@RequestBody statement: Statement) {
        repository.add(statement)
    }
}
