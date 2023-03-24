package eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence

import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchProblemQuery
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("orkg.features.pwc-legacy-model", havingValue = "false", matchIfMissing = true)
class FindResearchProblemQueryAdapter(
    private val researchProblemRepository: ResearchProblemRepository
) : FindResearchProblemQuery {
    override fun findResearchProblemForDataset(datasetId: ThingId, pageable: Pageable): Page<ResearchProblem> {
        return researchProblemRepository.findResearchProblemForDataset(datasetId, pageable).map {
            ResearchProblem(
                it.id,
                it.label
            )
        }
    }
}
