package org.orkg.curation.domain

import org.orkg.curation.input.RetrieveCurationUseCase
import org.orkg.curation.output.CurationRepository
import org.orkg.graph.domain.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CurationService(private val curationRepository: CurationRepository) : RetrieveCurationUseCase {
    override fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<Predicate> {
       return curationRepository.findAllPredicatesWithoutDescriptions(pageable)
    }
}
