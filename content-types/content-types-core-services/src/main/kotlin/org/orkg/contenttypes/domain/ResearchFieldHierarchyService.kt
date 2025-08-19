package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ResearchFieldHierarchyUseCases
import org.orkg.contenttypes.output.ResearchFieldHierarchyRepository
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.orkg.graph.domain.Resource
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class ResearchFieldHierarchyService(
    private val repository: ResearchFieldHierarchyRepository,
    private val researchFieldRepository: ResearchFieldRepository,
) : ResearchFieldHierarchyUseCases {
    override fun findAllChildrenByAncestorId(id: ThingId, pageable: Pageable): Page<ResearchFieldWithChildCount> =
        researchFieldRepository.findById(id)
            .map { repository.findAllChildrenByAncestorId(id, pageable) }
            .orElseThrow { ResearchFieldNotFound(id) }

    override fun findAllParentsByChildId(id: ThingId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.findById(id)
            .map { repository.findAllParentsByChildId(id, pageable) }
            .orElseThrow { ResearchFieldNotFound(id) }

    override fun findAllRootsByDescendantId(id: ThingId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.findById(id)
            .map { repository.findAllRootsByDescendantId(id, pageable) }
            .orElseThrow { ResearchFieldNotFound(id) }

    override fun findAllRoots(pageable: Pageable): Page<Resource> =
        repository.findAllRoots(pageable)

    override fun findResearchFieldHierarchyByResearchFieldId(id: ThingId, pageable: Pageable): Page<ResearchFieldHierarchyEntry> =
        researchFieldRepository.findById(id)
            .map { repository.findResearchFieldHierarchyByResearchFieldId(id, pageable) }
            .orElseThrow { ResearchFieldNotFound(id) }
}
