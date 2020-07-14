package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import org.springframework.data.domain.Pageable
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
        getTopResearchProblemsGoingBack(listOf(1, 2, 3, 6), emptyList())
            .map(Neo4jResource::toResource)

    override fun getContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem> {
        return neo4jProblemRepository.getUsersLeaderboardPerProblem(problemId, pageable)
            .content
            .dropWhile { it.isAnonymous }
    }

    override fun getAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any> {
        return neo4jProblemRepository.getAuthorsLeaderboardPerProblem(problemId, pageable)
            .content
            .map {
                if (it.isLiteral)
                    object {
                        val author = it.author
                        val papers = it.papers
                    }
                else
                    object {
                        val author = it.authorResource.toResource()
                        val papers = it.papers
                    }
            }
    }

    /*
    Iterate over the list of months, and if no problems are found go back a bit more in time
    and if none found take all time results
     */
    private fun getTopResearchProblemsGoingBack(listOfMonths: List<Int>, result: List<Neo4jResource>): Iterable<Neo4jResource> {
        val month = listOfMonths.firstOrNull()
        val problems = if (month == null)
            neo4jProblemRepository.getTopResearchProblemsAllTime()
        else
            neo4jProblemRepository.getTopResearchProblemsGoingBack(month)
        val newResult = result.plus(problems).distinct()
        return if (newResult.count() >= 5)
            newResult.take(5)
        else
            getTopResearchProblemsGoingBack(listOfMonths.drop(1), newResult)
    }
}
