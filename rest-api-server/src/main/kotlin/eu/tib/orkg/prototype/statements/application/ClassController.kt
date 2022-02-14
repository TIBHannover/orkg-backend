package eu.tib.orkg.prototype.statements.application

import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.Resource
import java.net.URI
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
@RequestMapping("/api/classes/")
class ClassController(private val service: ClassUseCases, private val resourceService: ResourceUseCases) :
    BaseController() {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ClassId): Class =
        service
            .findById(id)
            .orElseThrow { ClassNotFound() }

    @GetMapping("/", params = ["uri"])
    fun findByURI(@RequestParam uri: URI): Class = service
            .findByURI(uri)
            .orElseThrow { ClassNotFound() }

    @GetMapping("/{id}/resources/")
    fun findResourcesWithClass(
        @PathVariable id: ClassId,
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("creator", required = false) creator: ContributorId?,
        pageable: Pageable
    ): Page<Resource> {
        return if (creator != null) {
            when {
                searchString == null -> resourceService.findAllByClassAndCreatedBy(pageable, id, creator)
                exactMatch -> resourceService.findAllByClassAndLabelAndCreatedBy(pageable, id, searchString, creator)
                else -> resourceService.findAllByClassAndLabelContainingAndCreatedBy(pageable, id, searchString, creator)
            }
        } else {
            when {
                searchString == null -> resourceService.findAllByClass(pageable, id)
                exactMatch -> resourceService.findAllByClassAndLabel(pageable, id, searchString)
                else -> resourceService.findAllByClassAndLabelContaining(pageable, id, searchString)
            }
        }
    }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        pageable: Pageable
    ): Page<Class> {
        return when {
            searchString == null -> service.findAll(pageable)
            exactMatch -> service.findAllByLabel(pageable, searchString)
            else -> service.findAllByLabelContaining(pageable, searchString)
        }
    }

    @PostMapping("/")
    @ResponseStatus(CREATED)
    fun add(@RequestBody `class`: CreateClassRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Any> {
        Label.ofOrNull(`class`.label) ?: throw InvalidLabel()
        if (`class`.id != null && service.findById(`class`.id).isPresent)
            throw ClassAlreadyExists(`class`.id.value)
        if (!`class`.hasValidName())
            throw ClassNotAllowed(`class`.id!!.value)
        if (`class`.uri != null) {
            val found = service.findByURI(`class`.uri)
            if (found.isPresent)
                throw DuplicateURI(`class`.uri, found.get().id.toString())
        }

        val userId = authenticatedUserId()
        val id = service.create(ContributorId(userId), `class`).id!!
        val location = uriComponentsBuilder
            .path("api/classes/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: ClassId,
        @RequestBody `class`: Class
    ): ResponseEntity<Class> {
        val found = service.findById(id)

        if (!found.isPresent)
            return ResponseEntity.notFound().build()

        var updatedClass = `class`.copy(id = found.get().id)

        updatedClass = when {
            updatedClass.label != `class`.label && updatedClass.uri != `class`.uri ->
                updatedClass.copy(label = `class`.label, uri = `class`.uri)
            updatedClass.label != `class`.label ->
                updatedClass.copy(label = `class`.label)
            updatedClass.uri != `class`.uri ->
                updatedClass.copy(uri = `class`.uri)
            else ->
                updatedClass
        }

        return ResponseEntity.ok(service.update(updatedClass))
    }
}

data class CreateClassRequest(
    val id: ClassId?,
    val label: String,
    val uri: URI?
) {
    /*
    Checks if the class has a valid class Id (class name)
    a valid class name is either null (auto assigned by the system)
    or a name that is not one of the reserved ones.
     */
    fun hasValidName(): Boolean =
        this.id == null || this.id.value !in listOf("Predicate", "Resource", "Class", "Literal", "Thing")
}
