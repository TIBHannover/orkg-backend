package org.orkg.curation.domain

import org.orkg.curation.input.RetrieveCurationUseCase
import org.orkg.curation.output.CurationRepository
import org.orkg.curation.domain.ResearchFieldPaperCount
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CurationService(private val curationRepository: CurationRepository) : RetrieveCurationUseCase {
    override fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<Predicate> {
       return curationRepository.findAllPredicatesWithoutDescriptions(pageable)
    }

    override fun findAllClassesWithoutDescriptions(pageable: Pageable): Page<Class> {
        return curationRepository.findAllClassesWithoutDescriptions(pageable)
    }

    override fun findAllPapersPerResearchField(pageable: Pageable): Page<ResearchFieldPaperCount>{
        return curationRepository.findAllPapersPerResearchField(pageable)
    }
}
