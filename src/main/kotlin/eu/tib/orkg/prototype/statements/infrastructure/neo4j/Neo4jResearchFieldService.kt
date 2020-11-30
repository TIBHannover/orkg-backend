package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResearchFieldRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jResearchFieldService(
    private val neo4jResearchFieldRepository: Neo4jResearchFieldRepository
) : ResearchFieldService {

    override fun getResearchProblemsOfField(fieldId: ResourceId, pageable: Pageable): Page<Any> {
        return neo4jResearchFieldRepository.getResearchProblemsOfField(fieldId, pageable)
            // .content
            .map {
                object {
                    val problem = it.problem.toResource()
                    val papers = it.papers
                }
            }
    }
}
