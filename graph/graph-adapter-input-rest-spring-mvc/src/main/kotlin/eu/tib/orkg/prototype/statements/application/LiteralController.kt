package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.LiteralRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.LiteralRepresentation
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
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
@RequestMapping("/api/literals/", produces = [MediaType.APPLICATION_JSON_VALUE])
class LiteralController(
    private val service: LiteralUseCases
) : BaseController(), LiteralRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): LiteralRepresentation =
        service.findById(id)
            .mapToLiteralRepresentation()
            .orElseThrow { LiteralNotFound(id) }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        pageable: Pageable
    ): Page<LiteralRepresentation> =
        when (searchString) {
            null -> service.findAll(pageable)
            else -> service.findAllByLabel(SearchString.of(searchString, exactMatch), pageable)
        }.mapToLiteralRepresentation()

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
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

        return created(location).body(service.findById(id).mapToLiteralRepresentation().get())
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody @Valid request: LiteralUpdateRequest
    ): LiteralRepresentation {
        var literal = service.findById(id).orElseThrow { LiteralNotFound(id) }

        if (request.label != null) {
            literal = literal.copy(label = request.label)
        }

        if (request.datatype != null) {
            if (request.datatype.isBlank()) throw PropertyIsBlank("datatype")
            literal = literal.copy(datatype = request.datatype)
        }
        service.update(literal)
        return findById(literal.id)
    }

    data class LiteralCreateRequest(
        // No restriction, as we need to support empty values; at lease for strings. See TIBHannover/orkg/orkg-backend!152.
        val label: String,
        @field:NotBlank
        val datatype: String = "xsd:string"
    )

    data class LiteralUpdateRequest(
        val id: ThingId?,
        val label: String?,
        val datatype: String?
    )
}
