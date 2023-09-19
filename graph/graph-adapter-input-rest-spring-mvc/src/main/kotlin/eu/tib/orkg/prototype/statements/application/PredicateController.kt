package eu.tib.orkg.prototype.statements.application

import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.PredicateRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.UpdatePredicateUseCase.ReplaceCommand
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
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
@RequestMapping("/api/predicates/", produces = [MediaType.APPLICATION_JSON_VALUE])
class PredicateController(
    private val service: PredicateUseCases
) : BaseController(), PredicateRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): PredicateRepresentation =
        service.findById(id).mapToPredicateRepresentation().orElseThrow { PredicateNotFound(id) }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        pageable: Pageable
    ): Page<PredicateRepresentation> =
        when (string) {
            null -> service.findAll(pageable)
            else -> service.findAllByLabel(SearchString.of(string, exactMatch), pageable)
        }.mapToPredicateRepresentation()

    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    fun add(
        @RequestBody predicate: CreatePredicateRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<PredicateRepresentation> {
        Label.ofOrNull(predicate.label) ?: throw InvalidLabel()
        if (predicate.id != null && service.findById(predicate.id).isPresent) throw PredicateAlreadyExists(predicate.id)
        val userId = authenticatedUserId()
        val id = service.create(
            CreatePredicateUseCase.CreateCommand(
                contributorId = ContributorId(userId),
                id = predicate.id?.value,
                label = predicate.label,
            )
        )

        val location = uriComponentsBuilder.path("api/predicates/{id}").buildAndExpand(id).toUri()

        return created(location).body(service.findById(id).mapToPredicateRepresentation().get())
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @RequestBody predicate: ReplacePredicateRequest
    ): ResponseEntity<PredicateRepresentation> {
        val found = service.findById(id)

        if (!found.isPresent) return ResponseEntity.notFound().build()

        service.update(id, ReplaceCommand(label = predicate.label, description = predicate.description))

        return ResponseEntity.ok(findById(id))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun delete(@PathVariable id: ThingId): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class ReplacePredicateRequest(
        val label: String,
        val description: String? = null,
    )
}

data class CreatePredicateRequest(
    val id: ThingId?,
    val label: String
)
