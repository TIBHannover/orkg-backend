package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.infrastructure.Neo4jEntity
import eu.tib.orkg.prototype.statements.infrastructure.Neo4jEntityRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/entities")
class EntityController(
    val repository: Neo4jEntityRepository
) {
    @PostMapping("/")
    fun add() {
        repository.save(Neo4jEntity())
    }

    @GetMapping("/")
    fun findAll(): MutableIterable<Neo4jEntity>? {
        return repository.findAll()
    }
}

@RestController
@RequestMapping("/api/statements")
class StatementController {
    @PostMapping("/")
    fun add(@RequestBody subj: Statement) {
        println(subj)
    }
}
