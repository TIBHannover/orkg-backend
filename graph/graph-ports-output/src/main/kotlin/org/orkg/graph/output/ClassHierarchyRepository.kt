package org.orkg.graph.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.domain.ChildClass
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.ClassHierarchyEntry
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ClassHierarchyRepository {
    fun findChildren(id: ThingId, pageable: Pageable): Page<ChildClass>

    fun findParent(id: ThingId): Optional<Class>

    fun findRoot(id: ThingId): Optional<Class>

    fun findAllRoots(pageable: Pageable): Page<Class>

    fun existsChild(id: ThingId, childId: ThingId): Boolean

    fun existsChildren(id: ThingId): Boolean

    fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry>

    fun countClassInstances(id: ThingId): Long

    fun findAllResourcesByLabelAndBaseClass(
        searchString: FuzzySearchString,
        baseClass: ThingId,
        pageable: Pageable
    ): Page<Resource>
}
