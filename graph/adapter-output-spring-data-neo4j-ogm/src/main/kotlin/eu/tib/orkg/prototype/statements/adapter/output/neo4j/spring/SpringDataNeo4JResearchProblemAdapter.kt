package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jAuthorPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jFieldPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4JResearchProblemAdapter(
    private val neo4jRepository: Neo4jProblemRepository
) : ResearchProblemRepository {
    override fun findById(id: ResourceId): Optional<Resource> =
        neo4jRepository.findById(id).map { it.toResource() }

    override fun findContributionsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findPapersByProblems(problemId, featured, unlisted, pageable)

    override fun findContributionsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findPapersByProblems(problemId, unlisted, pageable)

    override fun findPapersByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findPapersByProblems(problemId, featured, unlisted, pageable)

    override fun findPapersByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findPapersByProblems(problemId, unlisted, pageable)

    override fun findResearchFieldsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findResearchFieldsByProblems(problemId, featured, unlisted, pageable)

    override fun findResearchFieldsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findResearchFieldsByProblems(problemId, unlisted, pageable)

    override fun findComparisonsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findComparisonsByProblems(problemId, featured, unlisted, pageable)

    override fun findComparisonsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findComparisonsByProblems(problemId, unlisted, pageable)

    override fun findLiteratureListsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findLiteratureListsByProblems(problemId, featured, unlisted, pageable)

    override fun findLiteratureListsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findLiteratureListsByProblems(problemId, unlisted, pageable)

    override fun findSmartReviewsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findSmartReviewsByProblems(problemId, featured, unlisted, pageable)

    override fun findSmartReviewsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findSmartReviewsByProblems(problemId, unlisted, pageable)

    override fun findVisualizationsByProblems(
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findVisualizationsByProblems(problemId, featured, unlisted, pageable)

    override fun findVisualizationsByProblems(
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findVisualizationsByProblems(problemId, unlisted, pageable)

    override fun findResearchFieldsPerProblem(problemId: ResourceId): Iterable<ResearchProblemRepository.FieldPerProblem> =
        neo4jRepository.findResearchFieldsPerProblem(problemId).map { it.toFieldPerProblem() }

    override fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource> =
        neo4jRepository.findTopResearchProblemsGoingBack(months).map { it.toResource() }

    override fun findTopResearchProblemsAllTime(): Iterable<Resource> =
        neo4jRepository.findTopResearchProblemsAllTime().map { it.toResource() }

    override fun findContributorsLeaderboardPerProblem(
        problemId: ResourceId,
        pageable: Pageable
    ): Page<ResearchProblemRepository.ContributorPerProblem> =
        neo4jRepository.findContributorsLeaderboardPerProblem(problemId, pageable)

    override fun findAuthorsLeaderboardPerProblem(
        problemId: ResourceId,
        pageable: Pageable
    ): Page<ResearchProblemRepository.AuthorPerProblem> =
        neo4jRepository.findAuthorsLeaderboardPerProblem(problemId, pageable)
            .map { it.toAuthorPerProblem() }

    override fun findResearchProblemForDataset(datasetId: ResourceId): Iterable<Resource> =
        neo4jRepository.findResearchProblemForDataset(datasetId).map { it.toResource() }

    override fun findAllFeaturedProblems(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllFeaturedProblems(pageable).map { it.toResource() }

    override fun findAllNonFeaturedProblems(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllNonFeaturedProblems(pageable).map { it.toResource() }

    override fun findAllUnlistedProblems(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllUnlistedProblems(pageable).map { it.toResource() }

    override fun findAllListedProblems(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedProblems(pageable).map { it.toResource() }

    fun Neo4jFieldPerProblem.toFieldPerProblem() =
        ResearchProblemRepository.FieldPerProblem(
            field = field.toResource(),
            freq = freq
        )

    fun Neo4jAuthorPerProblem.toAuthorPerProblem() =
        ResearchProblemRepository.AuthorPerProblem(
            author = author,
            thing = thing.toThing(),
            papers = papers
        )
}
