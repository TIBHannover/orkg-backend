package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.spi.ResearchFieldRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "false", matchIfMissing = true)
class FindResearchFieldsQueryAdapter(
    private val repository: ResearchFieldRepository,
) : FindResearchFieldsQuery {
    override fun withBenchmarks(pageable: Pageable): Page<ResearchField> =
        repository.findResearchFieldsWithBenchmarks(pageable).map { ResearchField(it.id.value, it.label) }
}
