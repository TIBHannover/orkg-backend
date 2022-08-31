package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchProblemQuery
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "false", matchIfMissing = true)
class FindResearchProblemQueryAdapter(
    private val neo4jProblemRepository: Neo4jProblemRepository
) : FindResearchProblemQuery {
    override fun findResearchProblemForDataset(datasetId: ResourceId): List<ResearchProblem> {
        return neo4jProblemRepository.findResearchProblemForDataset(datasetId).map {
            ResearchProblem(
                it.resourceId!!,
                it.label!!
            )
        }
    }
}
