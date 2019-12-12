package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/literals/")
@CrossOrigin(origins = ["*"])
class LiteralController(private val service: LiteralService) : BaseController() {

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
        else if (exactMatch)
                service.findAllByLabel(searchString)
            else
                service.findAllByLabelContaining(searchString)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody literal: Literal, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Literal> {
        val userId = authenticatedUserId()
        val id = service.create(userId, literal.label).id
        val location = uriComponentsBuilder
            .path("api/literals/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: LiteralId,
        @RequestBody literal: Literal
    ): ResponseEntity<Literal> {
        val found = service.findById(id)

        if (!found.isPresent)
            return notFound().build()

        val updatedLiteral = literal.copy(id = found.get().id)

        return ok(service.update(updatedLiteral))
    }
}
