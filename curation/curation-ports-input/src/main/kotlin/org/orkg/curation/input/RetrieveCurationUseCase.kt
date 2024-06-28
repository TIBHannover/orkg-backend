package org.orkg.curation.input

import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Class
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveCurationUseCase {
    fun findAllPredicatesWithoutDescriptions(pageable: Pageable): Page<Predicate>
    fun findAllClassesWithoutDescriptions(pageable: Pageable): Page<Class>
}
