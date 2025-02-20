package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.DatasetUseCases
import org.orkg.contenttypes.input.ResearchProblemUseCases
import org.orkg.contenttypes.output.FindDatasetsQuery
import org.orkg.contenttypes.output.SummarizeDatasetQuery
import org.orkg.graph.input.ResourceUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class DatasetService(
    private val findDatasets: FindDatasetsQuery,
    private val researchProblemService: ResearchProblemUseCases,
    private val summarizeDataset: SummarizeDatasetQuery,
    private val resourceService: ResourceUseCases,
) : DatasetUseCases {
    override fun findAllDatasetsByResearchProblemId(id: ThingId, pageable: Pageable): Optional<Page<Dataset>> {
        val problem = researchProblemService.findById(id)
        if (!problem.isPresent) return Optional.empty()
        return Optional.of(findDatasets.findAllDatasetsByResearchProblemId(id, pageable))
    }

    override fun findAllDatasetSummariesByIdAndResearchProblemId(id: ThingId, problemId: ThingId, pageable: Pageable): Optional<Page<DatasetSummary>> {
        val dataset = resourceService.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        val problem = resourceService.findById(problemId)
        if (!problem.isPresent) return Optional.empty()
        return Optional.of(summarizeDataset.findAllDatasetSummariesByIdAndResearchProblemId(id, problemId, pageable))
    }
}
