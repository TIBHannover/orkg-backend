package org.orkg.contenttypes.adapter.output.neo4j

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jResearchFieldWithPaperCount
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jProblemRepository
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
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

    override fun findAllResearchFieldsWithPaperCountByProblemId(problemId: ThingId): Iterable<FieldWithFreq> =
        neo4jRepository.findAllResearchFieldsWithPaperCountByProblemId(problemId).map { it.toFieldWithFreq() }

    override fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource> =
        neo4jRepository.findTopResearchProblemsGoingBack(months).map { it.toResource() }

    override fun findTopResearchProblemsAllTime(): Iterable<Resource> =
        neo4jRepository.findTopResearchProblemsAllTime().map { it.toResource() }

    override fun findAllContributorsPerProblem(
        problemId: ThingId,
        pageable: Pageable
    ): Page<ContributorPerProblem> =
        neo4jRepository.findAllContributorsPerProblem(problemId, pageable)

    override fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllByDatasetId(datasetId, pageable).map { it.toResource() }

    override fun findAllListedProblems(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllListedProblems(pageable).map { it.toResource() }

    override fun findAllProblemsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllProblemsByVisibility(visibility, pageable).map { it.toResource() }

    fun Neo4jResearchFieldWithPaperCount.toFieldWithFreq() =
        FieldWithFreq(
            field = field.toResource(),
            freq = paperCount
        )
}
