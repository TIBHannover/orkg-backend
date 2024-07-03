package org.orkg.curation.input

import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Class
import org.orkg.curation.domain.ResearchFieldPaperCount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveCurationUseCase {
    fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<Predicate>
    fun findAllClassesWithoutDescriptions(pageable: Pageable): Page<Class>
    fun findAllPapersPerResearchField(pageable: Pageable): Page<ResearchFieldPaperCount>
}
