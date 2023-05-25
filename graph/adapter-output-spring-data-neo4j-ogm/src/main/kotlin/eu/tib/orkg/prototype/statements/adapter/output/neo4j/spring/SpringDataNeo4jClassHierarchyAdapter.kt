package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClass
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassHierarchyRepository
import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ChildClass
import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ClassHierarchyEntry
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jClassHierarchyAdapter(
    private val neo4jRepository: Neo4jClassHierarchyRepository
) : ClassHierarchyRepository {
    override fun findChildren(id: ThingId, pageable: Pageable): Page<ChildClass> =
        neo4jRepository.findChildren(id, pageable).map {
            ChildClass(it.`class`.toClass(), it.childCount)
        }

    override fun findParent(id: ThingId): Optional<Class> =
        neo4jRepository.findParent(id).map(Neo4jClass::toClass)

    override fun findRoot(id: ThingId): Optional<Class> =
        neo4jRepository.findRootClass(id).map(Neo4jClass::toClass)

    override fun findAllRoots(pageable: Pageable): Page<Class> =
        neo4jRepository.findAllRoots(pageable).map(Neo4jClass::toClass)

    override fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry> =
        neo4jRepository.findClassHierarchy(id, pageable).map {
            ClassHierarchyEntry(it.`class`.toClass(), if (it.parentId != null) ThingId(it.parentId) else null)
        }

    override fun countClassInstances(id: ThingId): Long =
        neo4jRepository.countClassInstances(id)

    override fun existsChild(id: ThingId, childId: ThingId): Boolean =
        neo4jRepository.existsChild(id, childId)

    override fun existsChildren(id: ThingId): Boolean =
        neo4jRepository.existsChildren(id)
}
