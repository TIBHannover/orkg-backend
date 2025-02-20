package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ResearchProblemRepository {
    fun findById(id: ThingId): Optional<Resource>

    fun findAllListedContributionsByProblem(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllContributionsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedPapersByProblem(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllPapersByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedResearchFieldsByProblem(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllResearchFieldsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedLiteratureListsByProblem(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllLiteratureListsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedSmartReviewsByProblem(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllSmartReviewsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedVisualizationsByProblem(id: ThingId, pageable: Pageable): Page<Resource>

    fun findAllVisualizationsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllResearchFieldsWithPaperCountByProblemId(problemId: ThingId): Iterable<FieldWithFreq>

    fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource>

    fun findTopResearchProblemsAllTime(): Iterable<Resource>

    fun findAllContributorsPerProblem(problemId: ThingId, pageable: Pageable): Page<ContributorPerProblem>

    fun findAllByDatasetId(datasetId: ThingId, pageable: Pageable): Page<Resource>
}
