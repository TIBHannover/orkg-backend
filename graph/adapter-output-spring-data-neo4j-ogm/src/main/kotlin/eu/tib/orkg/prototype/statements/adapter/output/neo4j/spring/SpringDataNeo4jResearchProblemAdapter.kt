package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jAuthorPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jContributorPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jDetailsPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jFieldPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jResearchProblemAdapter(
    private val neo4jRepository: Neo4jProblemRepository
) : ResearchProblemRepository {
    override fun findById(id: ThingId): Optional<Resource> =
        neo4jRepository.findById(id.toResourceId()).map { it.toResource() }

    override fun findContributionsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findPapersByProblems(problemId.toResourceId(), featured, unlisted, pageable)
            .map { it.toDetailsPerProblem() }


    override fun findContributionsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findPapersByProblems(problemId.toResourceId(), unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findPapersByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findPapersByProblems(problemId.toResourceId(), featured, unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findPapersByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findPapersByProblems(problemId.toResourceId(), unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findResearchFieldsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findResearchFieldsByProblems(problemId.toResourceId(), featured, unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findResearchFieldsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findResearchFieldsByProblems(problemId.toResourceId(), unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findComparisonsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findComparisonsByProblems(problemId.toResourceId(), featured, unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findComparisonsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findComparisonsByProblems(problemId.toResourceId(), unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findLiteratureListsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findLiteratureListsByProblems(problemId.toResourceId(), featured, unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findLiteratureListsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findLiteratureListsByProblems(problemId.toResourceId(), unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findSmartReviewsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findSmartReviewsByProblems(problemId.toResourceId(), featured, unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findSmartReviewsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findSmartReviewsByProblems(problemId.toResourceId(), unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findVisualizationsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findVisualizationsByProblems(problemId.toResourceId(), featured, unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findVisualizationsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<ResearchProblemRepository.DetailsPerProblem> =
        neo4jRepository.findVisualizationsByProblems(problemId.toResourceId(), unlisted, pageable)
            .map { it.toDetailsPerProblem() }

    override fun findResearchFieldsPerProblem(problemId: ThingId): Iterable<ResearchProblemRepository.FieldPerProblem> =
        neo4jRepository.findResearchFieldsPerProblem(problemId.toResourceId()).map { it.toFieldPerProblem() }

    override fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource> =
        neo4jRepository.findTopResearchProblemsGoingBack(months).map { it.toResource() }

    override fun findTopResearchProblemsAllTime(): Iterable<Resource> =
        neo4jRepository.findTopResearchProblemsAllTime().map { it.toResource() }

    override fun findContributorsLeaderboardPerProblem(
        problemId: ThingId,
        pageable: Pageable
    ): Page<ResearchProblemRepository.ContributorPerProblem> =
        neo4jRepository.findContributorsLeaderboardPerProblem(problemId.toResourceId(), pageable)
            .map { it.toContributorPerProblem() }

    override fun findResearchProblemForDataset(datasetId: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findResearchProblemForDataset(datasetId.toResourceId(), pageable).map { it.toResource() }

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

    fun Neo4jDetailsPerProblem.toDetailsPerProblem() =
        ResearchProblemRepository.DetailsPerProblem(
            id = id?.let { ThingId(it) },
            label = label,
            createdAt = createdAt,
            featured = featured,
            unlisted= unlisted,
            classes = classes,
            createdBy = createdBy
        )

    fun Neo4jContributorPerProblem.toContributorPerProblem() =
        ResearchProblemRepository.ContributorPerProblem(
            user = user,
            freq = freq
        )
}
