package org.orkg.graph.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.ChildClass
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ClassHierarchyEntry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveClassHierarchyUseCase {
    fun findAllChildrenByAncestorId(id: ThingId, pageable: Pageable): Page<ChildClass>

    fun findParentByChildId(id: ThingId): Optional<Class>

    fun findRootByDescendantId(id: ThingId): Optional<Class>

    fun findAllRoots(pageable: Pageable): Page<Class>

    fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry>

    fun countClassInstances(id: ThingId): Long
}
