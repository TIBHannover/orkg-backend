package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel

import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.legacymodel.neo4j.LegacyNeo4jProblemRepository
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchProblemQuery
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.toResourceId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "true")
class LegacyFindResearchProblemQueryAdapter(
    private val legacyNeo4jProblemRepository: LegacyNeo4jProblemRepository
) : FindResearchProblemQuery {
    override fun findResearchProblemForDataset(datasetId: ThingId): List<ResearchProblem> {
        return legacyNeo4jProblemRepository.findResearchProblemForDataset(datasetId.toResourceId()).map {
            ResearchProblem(
                ThingId(it.resourceId!!.value),
                it.label!!
            )
        }
    }
}
