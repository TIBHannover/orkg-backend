package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.RetrieveDatasetUseCase
import org.orkg.contenttypes.input.RetrieveResearchProblemUseCase
import org.orkg.contenttypes.output.FindDatasetsQuery
import org.orkg.contenttypes.output.SummarizeDatasetQuery
import org.orkg.graph.input.ResourceUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class DatasetService(
    private val findDatasets: FindDatasetsQuery,
    private val researchProblemService: RetrieveResearchProblemUseCase,
    private val summarizeDataset: SummarizeDatasetQuery,
    private val resourceService: ResourceUseCases
) : RetrieveDatasetUseCase {
    override fun forResearchProblem(id: ThingId, pageable: Pageable): Optional<Page<Dataset>> {
        val problem = researchProblemService.findById(id)
        if (!problem.isPresent) return Optional.empty()
        return Optional.of(findDatasets.forResearchProblem(id, pageable))
    }

    override fun summaryFor(id: ThingId, problemId: ThingId, pageable: Pageable): Optional<Page<DatasetSummary>> {
        val dataset = resourceService.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        val problem = resourceService.findById(problemId)
        if (!problem.isPresent) return Optional.empty()
        return Optional.of(summarizeDataset.byAndProblem(id, problemId, pageable))
    }
}
