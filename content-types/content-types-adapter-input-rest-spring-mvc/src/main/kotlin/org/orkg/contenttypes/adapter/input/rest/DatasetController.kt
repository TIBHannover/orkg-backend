package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.DatasetRepresentationAdapter
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.contenttypes.input.DatasetUseCases
import org.orkg.contenttypes.input.ResearchProblemUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class DatasetController(
    private val retrieveDatasets: DatasetUseCases,
    private val retrieveProblems: ResearchProblemUseCases,
) : DatasetRepresentationAdapter {
    @GetMapping("/api/datasets/research-problem/{id}")
    fun findAllDatasetsByResearchProblemId(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): Page<DatasetRepresentation> =
        retrieveDatasets.findAllDatasetsByResearchProblemId(id, pageable)
            .mapToDatasetRepresentation()

    @GetMapping("/api/datasets/{id}/problems")
    fun findAllResearchProblemsByDatasetId(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): Page<ResearchProblem> =
        retrieveProblems.findAllByDatasetId(id, pageable)

    @GetMapping("/api/datasets/{id}/problem/{problemId}/summary")
    fun findAllDatasetSummariesByIdAndResearchProblemId(
        @PathVariable id: ThingId,
        @PathVariable problemId: ThingId,
        pageable: Pageable,
    ): Page<DatasetSummaryRepresentation> =
        retrieveDatasets.findAllDatasetSummariesByIdAndResearchProblemId(id, problemId, pageable)
            .mapToDatasetSummaryRepresentation()
}
