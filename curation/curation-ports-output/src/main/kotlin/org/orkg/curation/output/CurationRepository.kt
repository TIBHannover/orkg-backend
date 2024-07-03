package org.orkg.curation.output

import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.orkg.curation.domain.ResearchFieldPaperCount

interface CurationRepository {
    fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<Predicate>
    fun findAllClassesWithoutDescriptions(pageable: Pageable): Page<Class>
    fun findAllPapersPerResearchField(pageable: Pageable): Page<ResearchFieldPaperCount>
}
