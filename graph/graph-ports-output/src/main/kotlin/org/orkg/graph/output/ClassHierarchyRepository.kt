package org.orkg.graph.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.ChildClass
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ClassHierarchyEntry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ClassHierarchyRepository {
    fun findAllChildrenByParentId(id: ThingId, pageable: Pageable): Page<ChildClass>

    fun findParentByChildId(id: ThingId): Optional<Class>

    fun findRootByDescendantId(id: ThingId): Optional<Class>

    fun findAllRoots(pageable: Pageable): Page<Class>

    fun existsChild(id: ThingId, childId: ThingId): Boolean

    fun existsChildren(id: ThingId): Boolean

    fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry>

    fun countClassInstances(id: ThingId): Long
}
