package eu.tib.orkg.prototype.statements.application

import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.AlreadyInUse
import eu.tib.orkg.prototype.statements.api.ClassRepresentation
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.InvalidURI
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.UpdateClassUseCase
import eu.tib.orkg.prototype.statements.api.UpdateNotAllowed
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Label
import java.net.URI
import javax.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import eu.tib.orkg.prototype.statements.api.ClassNotFound as ClassNotFoundProblem
import eu.tib.orkg.prototype.statements.api.InvalidLabel as InvalidLabelProblem

@RestController
@RequestMapping("/api/classes/")
class ClassController(private val service: ClassUseCases, private val resourceService: ResourceUseCases) :
    BaseController() {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ClassId): ClassRepresentation = service.findById(id).orElseThrow { ClassNotFound() }

    @GetMapping("/", params = ["uri"])
    fun findByURI(@RequestParam uri: URI): ClassRepresentation = service.findByURI(uri).orElseThrow { ClassNotFound() }

    @GetMapping("/", params = ["ids"])
    fun findByIds(@RequestParam ids: List<ClassId>, pageable: Pageable): Page<ClassRepresentation> =
        service.findAllById(ids, pageable)

    @GetMapping("/{id}/resources/")
    fun findResourcesWithClass(
        @PathVariable id: ClassId,
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("creator", required = false) creator: ContributorId?,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        return if (creator != null) {
            when {
                searchString == null -> resourceService.findAllByClassAndCreatedBy(pageable, id, creator)
                exactMatch -> resourceService.findAllByClassAndLabelAndCreatedBy(pageable, id, searchString, creator)
                else -> resourceService.findAllByClassAndLabelContainingAndCreatedBy(
                    pageable, id, searchString, creator
                )
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
    ): Page<ClassRepresentation> {
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
        if (`class`.id != null && service.findById(`class`.id).isPresent) throw ClassAlreadyExists(`class`.id.value)
        if (!`class`.hasValidName()) throw ClassNotAllowed(`class`.id!!.value)
        if (`class`.uri != null) {
            val found = service.findByURI(`class`.uri)
            if (found.isPresent) throw DuplicateURI(`class`.uri, found.get().id.toString())
        }

        val userId = authenticatedUserId()
        val id = service.create(ContributorId(userId), `class`).id
        val location = uriComponentsBuilder.path("api/classes/{id}").buildAndExpand(id).toUri()

        return created(location).body(service.findById(id).get())
    }

    @PutMapping("/{id}")
    fun replace(
        @PathVariable id: ClassId,
        @RequestBody request: ReplaceClassRequest
    ): ResponseEntity<ClassRepresentation> {
        // We will be very lenient with the ID, meaning we do not validate it. But we correct for it in the response. (For now.)
        val newValues = UpdateClassUseCase.ReplaceCommand(label = request.label, uri = request.uri)
        service.replace(id, newValues).onFailure {
            when (it.reason) {
                ClassNotFoundProblem -> throw ClassNotFound()
                InvalidLabelProblem -> throw InvalidLabel()
                InvalidURI -> throw IllegalStateException("An invalid URI got passed when replacing a class. This should not happen. Please report a bug.")
                UpdateNotAllowed -> throw CannotResetURI(id.value)
                AlreadyInUse -> throw URIAlreadyInUse(request.uri.toString())
            }
        }
        return ResponseEntity.ok(service.findById(id).get())
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: ClassId,
        @Valid @RequestBody requestBody: UpdateRequestBody
    ): ResponseEntity<Any> {
        if (requestBody.label != null) {
            service.updateLabel(id, requestBody.label).onFailure {
                when (it.reason) {
                    ClassNotFoundProblem -> throw ClassNotFound()
                    InvalidLabelProblem -> throw InvalidLabel()
                }
            }
        }
        if (requestBody.uri != null) {
            service.updateURI(id, requestBody.uri).onFailure {
                when (it.reason) {
                    ClassNotFoundProblem -> throw ClassNotFound()
                    InvalidURI -> throw InvalidURI()
                    UpdateNotAllowed -> throw CannotResetURI(id.value)
                    AlreadyInUse -> throw URIAlreadyInUse(requestBody.uri)
                }
            }
        }
        return ResponseEntity.ok(Unit)
    }

    data class UpdateRequestBody(
        val label: String? = null,
        val uri: String? = null,
    )

    data class ReplaceClassRequest(
        val label: String,
        val uri: URI? = null,
    )
}

data class CreateClassRequest(
    val id: ClassId?,
    val label: String,
    val uri: URI?
) {
    /*
    Checks if the class has a valid class ID (class name)
    a valid class name is either null (auto assigned by the system)
    or a name that is not one of the reserved ones.
     */
    fun hasValidName(): Boolean =
        this.id == null || this.id.value !in listOf("Predicate", "Resource", "Class", "Literal", "Thing")
}
