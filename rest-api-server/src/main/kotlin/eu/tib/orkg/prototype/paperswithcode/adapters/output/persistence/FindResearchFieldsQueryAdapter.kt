package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResearchFieldRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "false", matchIfMissing = true)
class FindResearchFieldsQueryAdapter(
    private val repository: Neo4jResearchFieldRepository,
) : FindResearchFieldsQuery {
    override fun withBenchmarks(): List<ResearchField> =
        repository.findResearchFieldsWithBenchmarks().map { ResearchField(it.resourceId!!.value, it.label!!) }
}
