package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.FieldService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jFieldRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jFieldService(
    private val neo4jFieldRepository: Neo4jFieldRepository
) : FieldService {

    override fun getResearchProblemsOfField(fieldId: ResourceId): List<Any> {
        return neo4jFieldRepository.getResearchProblemsOfField(fieldId)
            .map {
                object {
                    val problem = it.problem.toResource()
                    val papers = it.papers
                }
            }
    }
}
