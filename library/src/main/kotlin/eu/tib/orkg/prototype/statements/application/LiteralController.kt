package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.LiteralRepresentation
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
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
class LiteralController(
    private val service: LiteralUseCases,
    private val repository: LiteralRepository, // FIXME: Work-around, needs rewrite in service
) : BaseController() {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: LiteralId): LiteralRepresentation =
        service
            .findById(id)
            .orElseThrow { LiteralNotFound(id) }

    // TODO: remove when front-end has migrated to paged method
    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean
    ): Iterable<LiteralRepresentation> =
        findByLabel(searchString, exactMatch, PageRequest.of(0, Int.MAX_VALUE)).content

    @GetMapping("/", params = ["size"])
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        pageable: Pageable
    ): Page<LiteralRepresentation> =
        if (searchString == null)
            service.findAll(pageable)
        else if (exactMatch)
            service.findAllByLabel(searchString, pageable)
        else
            service.findAllByLabelContaining(searchString, pageable)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(
        @RequestBody @Valid literal: LiteralCreateRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<LiteralRepresentation> {
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
    ): ResponseEntity<LiteralRepresentation> {
        var literal = repository.findByLiteralId(id).orElseThrow { LiteralNotFound(id) }

        if (request.label != null) {
            literal = literal.copy(label = request.label)
        }

        if (request.datatype != null) {
            if (request.datatype.isBlank()) throw PropertyIsBlank("datatype")
            literal = literal.copy(datatype = request.datatype)
        }
        return ok(service.update(literal))
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
