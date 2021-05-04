package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jResearchFieldService
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class ResearchFieldAdapter(
    val findResearchFields: Neo4jResearchFieldService
) : FindResearchFieldsQuery {
    override fun withBenchmarks(): List<ResearchField> =
        findResearchFields.withBenchmarks()
}
