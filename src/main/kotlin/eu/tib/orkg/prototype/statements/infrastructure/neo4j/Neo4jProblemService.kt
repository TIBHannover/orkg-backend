package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
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

    override fun getTopResearchProblems(): List<Resource> =
        getTopResearchProblemsGoingBack(listOf(1, 2, 3, 6))
            .map(Neo4jResource::toResource)

    /*
    Iterate over the list of months, and if no problems are found go back a bit more in time
    and if none found take all time results
     */
    private fun getTopResearchProblemsGoingBack(listOfMonths: List<Int>): Iterable<Neo4jResource> {
        val month = listOfMonths.firstOrNull() ?: return neo4jProblemRepository.getTopResearchProblemsAllTime()
        val problems = neo4jProblemRepository.getTopResearchProblemsGoingBack(month)
        return if (problems.count() > 0)
            problems
        else
            getTopResearchProblemsGoingBack(listOfMonths.drop(1))
    }
}
