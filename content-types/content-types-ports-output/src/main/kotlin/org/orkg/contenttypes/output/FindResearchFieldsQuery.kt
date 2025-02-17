package org.orkg.contenttypes.output

import org.orkg.contenttypes.domain.ResearchField
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FindResearchFieldsQuery {
    /**
     * Find all research fields that have benchmarks.
     *
     * @return This list of research fields, or an empty list otherwise.
     */
    fun findAllWithBenchmarks(pageable: Pageable): Page<ResearchField>
}
