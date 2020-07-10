package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jProblemService(
    private val neo4jProblemRepository: Neo4jProblemRepository
) : ProblemService {

    override fun getFieldsPerProblem(problemId: ResourceId): List<Any> {
        return neo4jProblemRepository.getResearchFieldsPerProblem(problemId).map {
            object {
                val field = it.field.toResource()
                val freq = it.freq
            }
        }
    }
}
