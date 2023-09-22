package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.statements.api.ResearchFieldHierarchyUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldWithChildCount
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldHierarchyUseCase.ResearchFieldHierarchyEntry
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchFieldHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ResearchFieldRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ResearchFieldHierarchyService(
    private val repository: ResearchFieldHierarchyRepository,
    private val researchFieldRepository: ResearchFieldRepository
) : ResearchFieldHierarchyUseCases {

    override fun findChildren(id: ThingId, pageable: Pageable): Page<ResearchFieldWithChildCount> =
        researchFieldRepository.findById(id)
            .map { repository.findChildren(id, pageable) }
            .orElseThrow { ResearchFieldNotFound(id) }

    override fun findParents(id: ThingId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.findById(id)
            .map { repository.findParents(id, pageable) }
            .orElseThrow { ResearchFieldNotFound(id) }

    override fun findRoots(id: ThingId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.findById(id)
            .map { repository.findRoots(id, pageable) }
            .orElseThrow { ResearchFieldNotFound(id) }

    override fun findAllRoots(pageable: Pageable): Page<Resource> =
        repository.findAllRoots(pageable)

    override fun findResearchFieldHierarchy(id: ThingId, pageable: Pageable): Page<ResearchFieldHierarchyEntry> =
        researchFieldRepository.findById(id)
            .map { repository.findResearchFieldHierarchy(id, pageable) }
            .orElseThrow { ResearchFieldNotFound(id) }
}
