package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveClassHierarchyUseCase {
    fun findChildren(id: ThingId, pageable: Pageable): Page<ChildClass>

    fun findParent(id: ThingId): Optional<Class>

    fun findRoot(id: ThingId): Optional<Class>

    fun findAllRoots(pageable: Pageable): Page<Class>

    fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry>

    fun countClassInstances(id: ThingId): Long

    data class ChildClass(
        val `class`: Class,
        val childCount: Long
    )

    data class ClassHierarchyEntry(
        val `class`: Class,
        val parentId: ThingId?
    )
}
