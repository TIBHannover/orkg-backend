package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ChildClass
import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ClassHierarchyEntry
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
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
}
