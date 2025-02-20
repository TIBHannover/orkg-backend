package org.orkg.curation.domain

import org.orkg.curation.input.CurationUseCases
import org.orkg.curation.output.CurationRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CurationService(private val curationRepository: CurationRepository) : CurationUseCases {
    override fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<Predicate> =
        curationRepository.findAllPredicatesWithoutDescriptions(pageable)

    override fun findAllClassesWithoutDescriptions(pageable: Pageable): Page<Class> =
        curationRepository.findAllClassesWithoutDescriptions(pageable)
}
