package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jContributorPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jFieldPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase.*
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
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
        neo4jRepository.findById(id).map { it.toResource() }

    override fun findAllListedContributionsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedContributionsByProblem(id, pageable).map { it.toResource() }

    override fun findAllContributionsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllContributionsByProblemAndVisibility(id, visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedPapersByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedPapersByProblem(id, pageable).map { it.toResource() }

    override fun findAllPapersByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllPapersByProblemAndVisibility(id, visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedResearchFieldsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedResearchFieldsByProblem(id, pageable).map { it.toResource() }

    override fun findAllResearchFieldsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllResearchFieldsByProblemAndVisibility(id, visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedComparisonsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedComparisonsByProblem(id, pageable).map { it.toResource() }

    override fun findAllComparisonsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllComparisonsByProblemAndVisibility(id, visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedLiteratureListsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedLiteratureListsByProblem(id, pageable).map { it.toResource() }

    override fun findAllLiteratureListsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllLiteratureListsByProblemAndVisibility(id, visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedSmartReviewsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedSmartReviewsByProblem(id, pageable).map { it.toResource() }

    override fun findAllSmartReviewsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllSmartReviewsByProblemAndVisibility(id, visibility, pageable)
            .map { it.toResource() }

    override fun findAllListedVisualizationsByProblem(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedVisualizationsByProblem(id, pageable).map { it.toResource() }

    override fun findAllVisualizationsByProblemAndVisibility(
        id: ThingId,
        visibility: Visibility,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.findAllVisualizationsByProblemAndVisibility(id, visibility, pageable)
            .map { it.toResource() }

    override fun findResearchFieldsPerProblem(problemId: ThingId): Iterable<FieldWithFreq> =
        neo4jRepository.findResearchFieldsPerProblem(problemId).map { it.toFieldWithFreq() }

    override fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource> =
        neo4jRepository.findTopResearchProblemsGoingBack(months).map { it.toResource() }

    override fun findTopResearchProblemsAllTime(): Iterable<Resource> =
        neo4jRepository.findTopResearchProblemsAllTime().map { it.toResource() }

    override fun findContributorsLeaderboardPerProblem(
        problemId: ThingId,
        pageable: Pageable
    ): Page<ResearchProblemRepository.ContributorPerProblem> =
        neo4jRepository.findContributorsLeaderboardPerProblem(problemId, pageable)
            .map { it.toContributorPerProblem() }

    override fun findResearchProblemForDataset(datasetId: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findResearchProblemForDataset(datasetId, pageable).map { it.toResource() }

    override fun findAllListedProblems(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedProblems(pageable).map { it.toResource() }

    override fun findAllProblemsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllProblemsByVisibility(visibility, pageable).map { it.toResource() }

    fun Neo4jFieldPerProblem.toFieldWithFreq() =
        FieldWithFreq(
            field = field.toResource(),
            freq = freq
        )

    fun Neo4jContributorPerProblem.toContributorPerProblem() =
        ResearchProblemRepository.ContributorPerProblem(
            user = user,
            freq = freq
        )
}
