package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.spi.ResearchFieldRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "false", matchIfMissing = true)
class FindResearchFieldsQueryAdapter(
    private val repository: ResearchFieldRepository,
) : FindResearchFieldsQuery {
    override fun withBenchmarks(): List<ResearchField> =
        repository.findResearchFieldsWithBenchmarks().map { ResearchField(it.id.value, it.label) }
}
