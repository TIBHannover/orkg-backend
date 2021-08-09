package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jProblemService(
    private val neo4jProblemRepository: Neo4jProblemRepository,
    private val neo4jResourceRepository: Neo4jResourceRepository
) : ProblemService {
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

    override fun getFeaturedProblemFlag(id: ResourceId): Boolean {
        val result = neo4jProblemRepository.findById(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured
    }

    override fun getUnlistedProblemFlag(id: ResourceId): Boolean {
        val result = neo4jProblemRepository.findById(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted
    }

    override fun loadFeaturedProblems(pageable: Pageable): Page<Resource> =
        neo4jProblemRepository.findAllFeaturedProblems(pageable)
            .map(Neo4jResource::toResource)

    override fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource> =
        neo4jProblemRepository.findAllNonFeaturedProblems(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnlistedProblems(pageable: Pageable):
        Page<Resource> =
        neo4jProblemRepository.findAllUnlistedProblems(pageable)
            .map(Neo4jResource::toResource)

    override fun loadListedProblems(pageable: Pageable):
        Page<Resource> =
        neo4jProblemRepository.findAllListedProblems(pageable)
            .map(Neo4jResource::toResource)

    override fun markAsFeatured(resourceId: ResourceId): Optional<Resource> {
        setUnlistedFlag(resourceId, false)
        return setFeaturedFlag(resourceId, true)
    }

    override fun markAsNonFeatured(resourceId: ResourceId) = setFeaturedFlag(resourceId, false)

    override fun markAsUnlisted(resourceId: ResourceId): Optional<Resource> {
        setFeaturedFlag(resourceId, false)
        return setUnlistedFlag(resourceId, true)
    }

    override fun markAsListed(resourceId: ResourceId) = setUnlistedFlag(resourceId, false)

    private fun setFeaturedFlag(resourceId: ResourceId, featured: Boolean): Optional<Resource> {
        val result = neo4jProblemRepository.findById(resourceId)
        if (result.isPresent) {
            val problem = result.get()
            problem.featured = featured
            return Optional.of(neo4jProblemRepository.save(problem).toResource())
        }
        return Optional.empty()
    }

    private fun setUnlistedFlag(resourceId: ResourceId, unlisted: Boolean): Optional<Resource> {
        val result = neo4jProblemRepository.findById(resourceId)
        if (result.isPresent) {
            val problem = result.get()
            problem.unlisted = unlisted
            return Optional.of(neo4jProblemRepository.save(problem).toResource())
        }
        return Optional.empty()
    }
}
