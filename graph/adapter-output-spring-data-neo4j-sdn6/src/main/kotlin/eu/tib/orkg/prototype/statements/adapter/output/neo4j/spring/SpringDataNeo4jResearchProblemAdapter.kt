package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jAuthorPerProblem
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

    override fun findAllListedContributionsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedContributionsByProblem(id.toResourceId(), pageable).map { it.toResource() }

    override fun findAllContributionsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllContributionsByProblemAndVisibility(id.toResourceId(), visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedPapersByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedPapersByProblem(id.toResourceId(), pageable).map { it.toResource() }

    override fun findAllPapersByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllPapersByProblemAndVisibility(id.toResourceId(), visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedResearchFieldsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedResearchFieldsByProblem(id.toResourceId(), pageable).map { it.toResource() }

    override fun findAllResearchFieldsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllResearchFieldsByProblemAndVisibility(id.toResourceId(), visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedComparisonsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedComparisonsByProblem(id.toResourceId(), pageable).map { it.toResource() }

    override fun findAllComparisonsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllComparisonsByProblemAndVisibility(id.toResourceId(), visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedLiteratureListsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedLiteratureListsByProblem(id.toResourceId(), pageable).map { it.toResource() }

    override fun findAllLiteratureListsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllLiteratureListsByProblemAndVisibility(id.toResourceId(), visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedSmartReviewsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedSmartReviewsByProblem(id.toResourceId(), pageable).map { it.toResource() }

    override fun findAllSmartReviewsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllSmartReviewsByProblemAndVisibility(id.toResourceId(), visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedVisualizationsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedVisualizationsByProblem(id.toResourceId(), pageable).map { it.toResource() }

    override fun findAllVisualizationsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllVisualizationsByProblemAndVisibility(id.toResourceId(), visibility, pageable)
            .map { it.toResource() }

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

    override fun findResearchProblemForDataset(datasetId: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findResearchProblemForDataset(datasetId.toResourceId(), pageable).map { it.toResource() }

    override fun findAllListedProblems(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedProblems(pageable).map { it.toResource() }

    override fun findAllProblemsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllProblemsByVisibility(visibility, pageable).map { it.toResource() }

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
