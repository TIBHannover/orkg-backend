package org.orkg.curation.output

import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CurationRepository {
    fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<Predicate>

    fun findAllClassesWithoutDescriptions(pageable: Pageable): Page<Class>
}
