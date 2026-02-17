package org.orkg.contenttypes.domain

import org.orkg.contenttypes.input.LegacyResearchFieldUseCases
import org.orkg.contenttypes.output.LegacyFindResearchFieldsQuery
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class LegacyResearchFieldService(
    private val researchFieldsQuery: LegacyFindResearchFieldsQuery,
) : LegacyResearchFieldUseCases {
    override fun findAllWithBenchmarks(pageable: Pageable): Page<ResearchField> =
        researchFieldsQuery.findAllWithBenchmarks(pageable)
}
