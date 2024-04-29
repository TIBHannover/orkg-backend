package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchFieldHierarchyEntry
import org.orkg.contenttypes.domain.ResearchFieldWithChildCount
import org.orkg.graph.domain.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchFieldHierarchyUseCase {
    fun findChildren(id: ThingId, pageable: Pageable): Page<ResearchFieldWithChildCount>

    fun findParents(id: ThingId, pageable: Pageable): Page<Resource>

    fun findRoots(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllRoots(pageable: Pageable): Page<Resource>

    fun findResearchFieldHierarchy(id: ThingId, pageable: Pageable): Page<ResearchFieldHierarchyEntry>

}
