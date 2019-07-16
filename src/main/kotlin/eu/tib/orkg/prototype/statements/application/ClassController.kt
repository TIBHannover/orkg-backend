package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
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
import java.net.URI

@RestController
@RequestMapping("/api/classes/")
@CrossOrigin(origins = ["*"])
class ClassController(private val service: ClassService) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ClassId): Class =
        service
            .findById(id)
            .orElseThrow { ClassNotFound() }

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
    fun add(@RequestBody `class`: CreateClassRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Class> {
        val id = service.create(`class`).id
        val location = uriComponentsBuilder
            .path("api/classes/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }
}

data class CreateClassRequest(
    val id: ClassId?,
    val label: String,
    val uri: URI?
)
