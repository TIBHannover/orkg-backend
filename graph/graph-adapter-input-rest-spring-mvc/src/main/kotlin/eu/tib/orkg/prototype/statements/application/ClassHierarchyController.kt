package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.ChildClassRepresentationAdapter
import eu.tib.orkg.prototype.statements.ClassHierarchyEntryRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.ChildClassRepresentation
import eu.tib.orkg.prototype.statements.api.ClassHierarchyEntryRepresentation
import eu.tib.orkg.prototype.statements.api.ClassHierarchyUseCases
import eu.tib.orkg.prototype.statements.api.ClassRepresentation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/classes", produces = [MediaType.APPLICATION_JSON_VALUE])
class ClassHierarchyController(
    private val service: ClassHierarchyUseCases
) : BaseController(), ClassHierarchyEntryRepresentationAdapter, ChildClassRepresentationAdapter {

    @GetMapping("/{id}/children")
    fun findChildren(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ChildClassRepresentation> = service.findChildren(id, pageable).mapToChildClassRepresentation()

    @GetMapping("/{id}/parent")
    fun findParent(
        @PathVariable id: ThingId
    ): ResponseEntity<ClassRepresentation> = service.findParent(id)
        .mapToClassRepresentation()
        .map(::ok)
        .orElseGet { noContent().build() }

    @DeleteMapping("/{id}/parent")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun deleteParentRelation(
        @PathVariable id: ThingId,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        service.delete(id)
        return noContent().build()
    }

    @PostMapping("/{id}/parent", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun postParentRelation(
        @PathVariable id: ThingId,
        @RequestBody request: CreateParentRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        createRelations(request.parentId, setOf(id), false)
        val location = uriComponentsBuilder
            .path("api/classes/{id}/parent")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @GetMapping("/{id}/root")
    fun findRoot(
        @PathVariable id: ThingId
    ): ResponseEntity<ClassRepresentation> = service.findRoot(id)
        .mapToClassRepresentation()
        .map(::ok)
        .orElseGet { noContent().build() }

    @GetMapping("/roots")
    fun findAllRoots(
        pageable: Pageable
    ): Page<ClassRepresentation> = service.findAllRoots(pageable).mapToClassRepresentation()

    @PostMapping("/{id}/children", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun postChildrenRelation(
        @PathVariable id: ThingId,
        @RequestBody request: CreateChildrenRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        createRelations(id, request.childIds, true)
        val location = uriComponentsBuilder
            .path("api/classes/{id}/children")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PatchMapping("/{id}/children", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun patchChildrenRelation(
        @PathVariable id: ThingId,
        @RequestBody request: CreateChildrenRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        createRelations(id, request.childIds, false)
        val location = uriComponentsBuilder
            .path("api/classes/{id}/children")
            .buildAndExpand(id)
            .toUri()
        return ok().location(location).build()
    }

    private fun createRelations(parentId: ThingId, childIds: Set<ThingId>, checkIfParentIsLeaf: Boolean) {
        if (childIds.isEmpty()) throw EmptyChildIds()
        val userId = authenticatedUserId()
        service.create(ContributorId(userId), parentId, childIds, checkIfParentIsLeaf)
    }

    @GetMapping("/{id}/count")
    fun countClassInstances(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): CountResponse = CountResponse(service.countClassInstances(id))

    @GetMapping("/{id}/hierarchy")
    fun findClassHierarchy(
        @PathVariable id: ThingId,
        pageable: Pageable
    ): Page<ClassHierarchyEntryRepresentation> =
        service.findClassHierarchy(id, pageable).mapToClassHierarchyEntryRepresentation()
}

data class CreateChildrenRequest(
    @field:JsonProperty("child_ids")
    val childIds: Set<ThingId>
)

data class CountResponse(
    @field:JsonProperty("count")
    val count: Long
)

data class CreateParentRequest(
    @field:JsonProperty("parent_id")
    val parentId: ThingId
)
