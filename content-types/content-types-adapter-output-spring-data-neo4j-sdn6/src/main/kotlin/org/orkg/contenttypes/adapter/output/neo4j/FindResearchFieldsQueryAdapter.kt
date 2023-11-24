package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.contenttypes.domain.ResearchField
import org.orkg.contenttypes.output.FindResearchFieldsQuery
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class FindResearchFieldsQueryAdapter(
    private val repository: ResearchFieldRepository,
) : FindResearchFieldsQuery {
    override fun withBenchmarks(pageable: Pageable): Page<ResearchField> =
        repository.findResearchFieldsWithBenchmarks(pageable).map { ResearchField(it.id.value, it.label) }
}
