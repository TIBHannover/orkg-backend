package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.contenttypes.domain.ResearchField
import org.orkg.contenttypes.output.LegacyFindResearchFieldsQuery
import org.orkg.contenttypes.output.LegacyResearchFieldRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class LegacyFindResearchFieldsQueryAdapter(
    private val repository: LegacyResearchFieldRepository,
) : LegacyFindResearchFieldsQuery {
    override fun findAllWithBenchmarks(pageable: Pageable): Page<ResearchField> =
        repository.findAllWithBenchmarks(pageable).map { ResearchField(it.id.value, it.label) }
}
