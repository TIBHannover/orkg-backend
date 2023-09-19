package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchFieldHierarchyUseCase {
    fun findChildren(id: ThingId, pageable: Pageable): Page<ResearchFieldWithChildCount>

    fun findParents(id: ThingId, pageable: Pageable): Page<Resource>

    fun findRoots(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllRoots(pageable: Pageable): Page<Resource>

    fun findResearchFieldHierarchy(id: ThingId, pageable: Pageable): Page<ResearchFieldHierarchyEntry>

    data class ResearchFieldWithChildCount(
        val resource: Resource,
        val childCount: Long
    )

    data class ResearchFieldHierarchyEntry(
        val resource: Resource,
        val parentIds: Set<ThingId>
    )
}
