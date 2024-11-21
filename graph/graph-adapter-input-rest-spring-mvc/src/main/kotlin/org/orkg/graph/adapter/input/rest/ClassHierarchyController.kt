package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.annotations.RequireCuratorRole
import org.orkg.common.contributorId
import org.orkg.graph.adapter.input.rest.mapping.ChildClassRepresentationAdapter
import org.orkg.graph.adapter.input.rest.mapping.ClassHierarchyEntryRepresentationAdapter
import org.orkg.graph.domain.EmptyChildIds
import org.orkg.graph.input.ClassHierarchyUseCases
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.Authentication
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
    override val statementService: StatementUseCases,
    private val service: ClassHierarchyUseCases
) : ClassHierarchyEntryRepresentationAdapter, ChildClassRepresentationAdapter {

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
    @RequireCuratorRole
    fun deleteParentRelation(
        @PathVariable id: ThingId,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Any> {
        service.delete(id)
        return noContent().build()
    }

    @PostMapping("/{id}/parent", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun postParentRelation(
        @PathVariable id: ThingId,
        @RequestBody request: CreateParentRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        createRelations(request.parentId, setOf(id), false, currentUser.contributorId())
        val location = uriComponentsBuilder
            .path("/api/classes/{id}/parent")
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
    @RequireCuratorRole
    fun postChildrenRelation(
        @PathVariable id: ThingId,
        @RequestBody request: CreateChildrenRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        createRelations(id, request.childIds, true, currentUser.contributorId())
        val location = uriComponentsBuilder
            .path("/api/classes/{id}/children")
            .buildAndExpand(id)
            .toUri()
        return created(location).build()
    }

    @PatchMapping("/{id}/children", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @RequireCuratorRole
    fun patchChildrenRelation(
        @PathVariable id: ThingId,
        @RequestBody request: CreateChildrenRequest,
        uriComponentsBuilder: UriComponentsBuilder,
        currentUser: Authentication?,
    ): ResponseEntity<Any> {
        createRelations(id, request.childIds, false, currentUser.contributorId())
        val location = uriComponentsBuilder
            .path("/api/classes/{id}/children")
            .buildAndExpand(id)
            .toUri()
        return ok().location(location).build()
    }

    private fun createRelations(parentId: ThingId, childIds: Set<ThingId>, checkIfParentIsLeaf: Boolean, contributorId: ContributorId) {
        if (childIds.isEmpty()) throw EmptyChildIds()
        service.create(contributorId, parentId, childIds, checkIfParentIsLeaf)
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
    @JsonProperty("child_ids")
    val childIds: Set<ThingId>
)

data class CountResponse(
    @JsonProperty("count")
    val count: Long
)

data class CreateParentRequest(
    @JsonProperty("parent_id")
    val parentId: ThingId
)
