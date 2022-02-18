package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
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
class LiteralController(private val service: LiteralUseCases) : BaseController() {

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
    fun add(
        @RequestBody @Valid literal: LiteralCreateRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Literal> {
        val userId = authenticatedUserId()
        val id = service.create(ContributorId(userId), literal.label, literal.datatype).id
        val location = uriComponentsBuilder
            .path("api/literals/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: LiteralId,
        @RequestBody @Valid request: LiteralUpdateRequest
    ): ResponseEntity<Literal> {
        val found = service.findById(id)

        if (!found.isPresent)
            return notFound().build()

        var updatedLiteral = found.get()

        if (request.label != null) {
            updatedLiteral = updatedLiteral.copy(label = request.label)
        }

        if (request.datatype != null) {
            if (request.datatype.isBlank()) throw PropertyIsBlank("datatype")
            updatedLiteral = updatedLiteral.copy(datatype = request.datatype)
        }
        return ok(service.update(updatedLiteral))
    }

    data class LiteralCreateRequest(
        // No restriction, as we need to support empty values; at lease for strings. See TIBHannover/orkg/orkg-backend!152.
        val label: String,
        @field:NotBlank
        val datatype: String = "xsd:string"
    )

    data class LiteralUpdateRequest(
        val id: LiteralId?,
        val label: String?,
        val datatype: String?
    )
}
