package eu.tib.orkg.prototype.statements.api

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveClassHierarchyUseCase {
    fun findChildren(id: ThingId, pageable: Pageable): Page<ChildClassRepresentation>

    fun findParent(id: ThingId): Optional<ClassRepresentation>

    fun findRoot(id: ThingId): Optional<ClassRepresentation>

    fun findAllRoots(pageable: Pageable): Page<ClassRepresentation>

    fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntryRepresentation>

    fun countClassInstances(id: ThingId): Long

    data class ChildClassRepresentation(
        @get:JsonProperty("class")
        val `class`: ClassRepresentation,
        @get:JsonProperty("child_count")
        val childCount: Long
    )

    data class ClassHierarchyEntryRepresentation(
        @get:JsonProperty("class")
        val `class`: ClassRepresentation,
        @get:JsonProperty("parent_id")
        val parentId: ThingId?
    )
}
