package eu.tib.orkg.prototype.statements.application

import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.UpdatePredicateUseCase.ReplaceCommand
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
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
@RequestMapping("/api/predicates/")
class PredicateController(private val service: PredicateUseCases) : BaseController() {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: PredicateId): PredicateRepresentation =
        service.findById(id).orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        pageable: Pageable
    ): Page<PredicateRepresentation> {
        return when {
            searchString == null -> service.findAll(pageable)
            exactMatch -> service.findAllByLabel(searchString, pageable)
            else -> service.findAllByLabelContaining(searchString, pageable)
        }
    }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(
        @RequestBody predicate: CreatePredicateRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        Label.ofOrNull(predicate.label) ?: throw InvalidLabel()
        if (predicate.id != null && service.findById(predicate.id).isPresent) return ResponseEntity.badRequest()
            .body("Predicate id <${predicate.id}> already exists!")
        val userId = authenticatedUserId()
        val id = service.create(ContributorId(userId), predicate).id

        val location = uriComponentsBuilder.path("api/predicates/{id}").buildAndExpand(id).toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: PredicateId,
        @RequestBody predicate: ReplacePredicateRequest
    ): ResponseEntity<PredicateRepresentation> {
        val found = service.findById(id)

        if (!found.isPresent) return ResponseEntity.notFound().build()

        service.update(id, ReplaceCommand(label = predicate.label, description = predicate.description))

        return ResponseEntity.ok(findById(id))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun delete(@PathVariable id: PredicateId): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class ReplacePredicateRequest(
        val label: String,
        val description: String? = null,
    )
}

data class CreatePredicateRequest(
    val id: PredicateId?,
    val label: String
)
