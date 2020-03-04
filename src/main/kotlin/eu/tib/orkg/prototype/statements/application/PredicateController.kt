package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import org.springframework.http.HttpStatus.CREATED
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
@RequestMapping("/api/predicates/")
class PredicateController(private val service: PredicateService) : BaseController() {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: PredicateId): Predicate =
        service
            .findById(id)
            .orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean
    ): Iterable<Predicate> {
        val pagination = createPageable(page, items, sortBy, desc)
        return when {
            searchString == null -> service.findAll(pagination)
            exactMatch -> service.findAllByLabel(searchString, pagination)
            else -> service.findAllByLabelContaining(searchString, pagination)
        }
    }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody predicate: CreatePredicateRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        if (predicate.id != null && service.findById(predicate.id).isPresent)
            return ResponseEntity.badRequest().body("Predicate id <${predicate.id}> already exists!")
        val userId = authenticatedUserId()
        val id = service.create(userId, predicate).id

        val location = uriComponentsBuilder
            .path("api/predicates/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: PredicateId,
        @RequestBody predicate: Predicate
    ): ResponseEntity<Predicate> {
        val found = service.findById(id)

        if (!found.isPresent)
            return ResponseEntity.notFound().build()

        val updatedPredicate = predicate.copy(id = found.get().id)

        return ResponseEntity.ok(service.update(updatedPredicate))
    }
}

data class CreatePredicateRequest(
    val id: PredicateId?,
    val label: String
)
