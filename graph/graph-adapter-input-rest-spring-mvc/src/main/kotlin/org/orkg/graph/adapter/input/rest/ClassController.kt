package org.orkg.graph.adapter.input.rest

import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.ofOrNull
import java.net.URI
import javax.validation.Valid
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.PreAuthorizeUser
import org.orkg.common.contributorId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.mapping.ClassRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ResourceRepresentationAdapter
import org.orkg.graph.domain.CannotResetURI
import org.orkg.graph.domain.ClassAlreadyExists
import org.orkg.graph.domain.ClassNotAllowed
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.ClassNotModifiable
import org.orkg.graph.domain.DuplicateURI
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.AlreadyInUse
import org.orkg.graph.input.ClassRepresentation
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.InvalidURI
import org.orkg.graph.input.ResourceRepresentation
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.ClassNotModifiableProblem
import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.input.UpdateNotAllowed
import org.orkg.graph.output.FormattedLabelRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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
import org.orkg.graph.input.ClassNotFound as ClassNotFoundProblem
import org.orkg.graph.input.InvalidLabel as InvalidLabelProblem

@RestController
@RequestMapping("/api/classes/", produces = [MediaType.APPLICATION_JSON_VALUE])
class ClassController(
    private val service: ClassUseCases,
    private val resourceService: ResourceUseCases,
    override val statementService: StatementUseCases,
    override val formattedLabelRepository: FormattedLabelRepository,
    override val flags: FeatureFlagService
) : ClassRepresentationAdapter, ResourceRepresentationAdapter {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: ThingId): ClassRepresentation =
        service.findById(id).mapToClassRepresentation().orElseThrow { ClassNotFound.withThingId(id) }

    @GetMapping("/", params = ["uri"])
    fun findByURI(@RequestParam uri: URI): ClassRepresentation =
        service.findByURI(uri).mapToClassRepresentation().orElseThrow { ClassNotFound.withURI(uri) }

    @GetMapping("/", params = ["ids"])
    fun findByIds(@RequestParam ids: List<ThingId>, pageable: Pageable): Page<ClassRepresentation> =
        service.findAllById(ids, pageable).mapToClassRepresentation()

    @GetMapping("/{id}/resources/")
    fun findResourcesWithClass(
        @PathVariable id: ThingId,
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        @RequestParam("creator", required = false) creator: ContributorId?,
        @RequestParam("visibility", required = false) visibility: VisibilityFilter?,
        pageable: Pageable
    ): Page<ResourceRepresentation> {
        return resourceService.findAll(
            includeClasses = setOf(id),
            label = string?.let { SearchString.of(it, exactMatch) },
            createdBy = creator,
            visibility = visibility,
            pageable = pageable
        ).mapToResourceRepresentation()
    }

    @GetMapping("/")
    fun findByLabel(
        @RequestParam("q", required = false) string: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean,
        pageable: Pageable
    ): Page<ClassRepresentation> =
        when (string) {
            null -> service.findAll(pageable)
            else -> service.findAllByLabel(SearchString.of(string, exactMatch), pageable)
        }.mapToClassRepresentation()

    @PreAuthorizeUser
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    fun add(
        @RequestBody `class`: CreateClassRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        @AuthenticationPrincipal currentUser: UserDetails?,
    ): ResponseEntity<ClassRepresentation> {
        Label.ofOrNull(`class`.label) ?: throw InvalidLabel()
        if (`class`.id != null && service.findById(`class`.id).isPresent) throw ClassAlreadyExists(`class`.id.value)
        if (!`class`.hasValidName()) throw ClassNotAllowed(`class`.id!!.value)
        if (`class`.uri != null) {
            val found = service.findByURI(`class`.uri)
            if (found.isPresent) throw DuplicateURI(`class`.uri, found.get().id.toString())
        }

        val contributorId = currentUser.contributorId()
        val id = service.create(
            CreateClassUseCase.CreateCommand(
                contributorId = contributorId,
                id = `class`.id?.value,
                label = `class`.label,
                uri = `class`.uri,
            )
        )
        val location = uriComponentsBuilder.path("api/classes/{id}").buildAndExpand(id).toUri()

        return created(location).body(service.findById(id).mapToClassRepresentation().get())
    }

    @PreAuthorizeUser
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun replace(
        @PathVariable id: ThingId,
        @RequestBody request: ReplaceClassRequest
    ): ClassRepresentation {
        // We will be very lenient with the ID, meaning we do not validate it. But we correct for it in the response. (For now.)
        val newValues = UpdateClassUseCase.ReplaceCommand(label = request.label, uri = request.uri)
        service.replace(id, newValues).onFailure {
            when (it.reason) {
                ClassNotFoundProblem -> throw ClassNotFound.withThingId(id)
                InvalidLabelProblem -> throw InvalidLabel()
                InvalidURI -> throw IllegalStateException("An invalid URI got passed when replacing a class. This should not happen. Please report a bug.")
                UpdateNotAllowed -> throw CannotResetURI(id.value)
                AlreadyInUse -> throw URIAlreadyInUse(request.uri.toString())
                ClassNotModifiableProblem -> throw ClassNotModifiable(id)
            }
        }
        return service.findById(id).mapToClassRepresentation().get()
    }

    @PreAuthorizeUser
    @PatchMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @PathVariable id: ThingId,
        @Valid @RequestBody requestBody: UpdateRequestBody
    ): ResponseEntity<Any> {
        if (requestBody.label != null) {
            service.updateLabel(id, requestBody.label).onFailure {
                when (it.reason) {
                    ClassNotFoundProblem -> throw ClassNotFound.withThingId(id)
                    InvalidLabelProblem -> throw InvalidLabel()
                    ClassNotModifiableProblem -> throw ClassNotModifiable(id)
                }
            }
        }
        if (requestBody.uri != null) {
            service.updateURI(id, requestBody.uri).onFailure {
                when (it.reason) {
                    ClassNotFoundProblem -> throw ClassNotFound.withThingId(id)
                    InvalidURI -> throw org.orkg.graph.domain.InvalidURI()
                    UpdateNotAllowed -> throw CannotResetURI(id.value)
                    AlreadyInUse -> throw URIAlreadyInUse(requestBody.uri)
                    ClassNotModifiableProblem -> throw ClassNotModifiable(id)
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
    val id: ThingId?,
    val label: String,
    val uri: URI?
) {
    /*
    Checks if the class has a valid class ID (class name)
    a valid class name is either null (auto assigned by the system)
    or a name that is not one of the reserved ones.
     */
    fun hasValidName(): Boolean =
        this.id == null || this.id.value !in listOf("Predicate", "Resource", "Class", "Literal", "List", "Thing")
}
