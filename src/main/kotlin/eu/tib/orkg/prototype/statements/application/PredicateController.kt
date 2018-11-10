package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.*

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
        @RequestParam(
            "q",
            required = false
        ) searchString: String?
    ) = if (searchString == null)
        service.findAll()
    else
        service.findAllByLabelContaining(searchString)

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody predicate: Predicate, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Predicate> {
        val id = service.create(predicate.label).id

        val location = uriComponentsBuilder
            .path("api/predicates/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }
}
