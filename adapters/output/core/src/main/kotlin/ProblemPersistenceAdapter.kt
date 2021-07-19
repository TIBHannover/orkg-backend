package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.ports.ProblemRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ProblemPersistenceAdapter(
    private val neo4jProblemRepository: Neo4jProblemRepository
): ProblemRepository {
    override fun findById(id: ResourceId): Optional<Resource> =
        neo4jProblemRepository
            .findById(id)
            .map(Neo4jResource::toResource)

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
