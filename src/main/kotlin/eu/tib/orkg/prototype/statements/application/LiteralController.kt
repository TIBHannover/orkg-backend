package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.*

@RestController
@RequestMapping("/api/literals/")
@CrossOrigin(origins = ["*"])
class LiteralController(private val service: LiteralService) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: LiteralId): Literal =
        service
            .findById(id)
            .orElseThrow { LiteralNotFound() }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?
    ) =
        if (searchString == null)
            service.findAll()
        else
            service.findAllByLabelContaining(searchString)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody literal: Literal, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Literal> {
        val id = service.create(literal.label).id
        val location = uriComponentsBuilder
            .path("api/literals/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }
}
