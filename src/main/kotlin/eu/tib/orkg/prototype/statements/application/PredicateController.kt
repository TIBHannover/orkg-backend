package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/predicates/")
@CrossOrigin(origins = ["*"])
class PredicateController(private val service: PredicateService) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: PredicateId): Predicate =
        service
            .findById(id)
            .orElseThrow { ResourceNotFound() }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean
    ) = if (searchString == null)
        service.findAll()
    else if (exactMatch)
            service.findAllByLabel(searchString)
        else
            service.findAllByLabelContaining(searchString)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody predicate: CreatePredicateRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Predicate> {
        val id = service.create(predicate).id

        val location = uriComponentsBuilder
            .path("api/predicates/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }
}

data class CreatePredicateRequest(
    val id: PredicateId?,
    val label: String
)
