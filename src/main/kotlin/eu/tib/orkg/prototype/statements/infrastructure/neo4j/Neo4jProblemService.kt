package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jProblemService(
    private val neo4jProblemRepository: Neo4jProblemRepository
) : ProblemService {

    override fun findFieldsPerProblem(problemId: ResourceId): List<Any> {
        return neo4jProblemRepository.findResearchFieldsPerProblem(problemId).map {
            object {
                val field = it.field.toResource()
                val freq = it.freq
            }
        }
    }

    override fun findTopResearchProblems(): List<Resource> =
        findTopResearchProblemsGoingBack(listOf(1, 2, 3, 6), emptyList())
            .map(Neo4jResource::toResource)

    /*
    Iterate over the list of months, and if no problems are found go back a bit more in time
    and if none found take all time results
     */
    private fun findTopResearchProblemsGoingBack(listOfMonths: List<Int>, result: List<Neo4jResource>): Iterable<Neo4jResource> {
        val month = listOfMonths.firstOrNull()
        val problems = if (month == null)
            neo4jProblemRepository.findTopResearchProblemsAllTime()
        else
            neo4jProblemRepository.findTopResearchProblemsGoingBack(month)
        val newResult = result.plus(problems).distinct()
        return if (newResult.count() >= 5)
            newResult.take(5)
        else
            findTopResearchProblemsGoingBack(listOfMonths.drop(1), newResult)
    }

    override fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem> {
        return neo4jProblemRepository
            .findContributorsLeaderboardPerProblem(problemId, pageable)
            .content
    }

    override fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any> {
        return neo4jProblemRepository.findAuthorsLeaderboardPerProblem(problemId, pageable)
            .content
            .map {
                if (it.isLiteral)
                    object {
                        val author = it.author
                        val papers = it.papers
                    }
                else
                    object {
                        val author = it.toAuthorResource.toResource()
                        val papers = it.papers
                    }
            }
    }
}
