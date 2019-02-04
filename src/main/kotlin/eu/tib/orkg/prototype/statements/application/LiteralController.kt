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
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean
    ) =
        if (searchString == null)
            service.findAll()
        else
            if (exactMatch)
                service.findAllByLabel(searchString)
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

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: LiteralId, @RequestBody literal: Literal
    ): ResponseEntity<Literal> {
        val found = service.findById(id)

        if (!found.isPresent)
            return notFound().build()

        val updatedLiteral = literal.copy(id = found.get().id)

        return ok(service.update(updatedLiteral))
    }
}
