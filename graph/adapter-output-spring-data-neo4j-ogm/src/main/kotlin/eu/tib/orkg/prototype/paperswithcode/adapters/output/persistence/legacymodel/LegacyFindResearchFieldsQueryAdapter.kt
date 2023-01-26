package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j.LegacyNeo4jResearchFieldRepository
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "true")
class LegacyFindResearchFieldsQueryAdapter(
    private val repository: LegacyNeo4jResearchFieldRepository
) : FindResearchFieldsQuery {
    override fun withBenchmarks(): List<ResearchField> =
        repository.findResearchFieldsWithBenchmarks().map { ResearchField(it.resourceId!!.value, it.label!!) }
}
