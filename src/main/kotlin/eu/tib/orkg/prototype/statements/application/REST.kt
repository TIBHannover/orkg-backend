package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Statement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/entities")
class EntityController {
    @PostMapping("/")
    fun add() {
        TODO()
    }

    @GetMapping("/")
    fun findAll() {
        TODO()
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
