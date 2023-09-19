package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldWithChildCount
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldHierarchyEntry
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchFieldHierarchyRepository {
    fun findChildren(id: ThingId, pageable: Pageable): Page<ResearchFieldWithChildCount>

    fun findParents(id: ThingId, pageable: Pageable): Page<Resource>

    fun findRoots(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllRoots(pageable: Pageable): Page<Resource>

    fun findResearchFieldHierarchy(id: ThingId, pageable: Pageable): Page<ResearchFieldHierarchyEntry>
}
