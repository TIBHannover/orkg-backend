package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.orkg.contenttypes.domain.Dataset
import org.orkg.contenttypes.domain.DatasetSummary
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.contenttypes.domain.ResearchProblem
import org.orkg.contenttypes.input.RetrieveBenchmarkUseCase
import org.orkg.contenttypes.input.RetrieveDatasetUseCase
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.contenttypes.input.RetrieveResearchProblemUseCase
import org.orkg.graph.domain.DatasetNotFound
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.domain.ResearchProblemNotFound
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class BenchmarkController(
    private val retrieveResearchField: RetrieveResearchFieldUseCase,
    private val retrieveBenchmarks: RetrieveBenchmarkUseCase,
    private val retrieveDatasets: RetrieveDatasetUseCase,
    private val retrieveProblems: RetrieveResearchProblemUseCase
) {
    @GetMapping("/api/research-fields/benchmarks")
    fun getResearchFieldsWithBenchmarks(pageable: Pageable): Page<ResearchField> =
        retrieveResearchField.withBenchmarks(pageable)

    @GetMapping("/api/benchmarks/summary/research-field/{id}")
    fun getBenchmarkSummaryForResearchField(@PathVariable id: ThingId, pageable: Pageable): Page<BenchmarkSummary> =
        retrieveBenchmarks
            .summariesForResearchField(id, pageable)
            .orElseThrow { ResearchFieldNotFound(id) }

    @GetMapping("/api/benchmarks/summary/")
    fun getBenchmarkSummaries(pageable: Pageable): Page<BenchmarkSummary> =
        retrieveBenchmarks
            .summary(pageable)
            .orElseThrow { RuntimeException() }

    @GetMapping("/api/datasets/research-problem/{id}")
    fun getDatasetForResearchProblem(@PathVariable id: ThingId, pageable: Pageable): Page<Dataset> =
        retrieveDatasets
            .forResearchProblem(id, pageable)
            .orElseThrow { ResearchProblemNotFound(id) }

    @GetMapping("/api/datasets/{id}/problems")
    fun getResearchProblemsForDataset(@PathVariable id: ThingId, pageable: Pageable): Page<ResearchProblem> =
        retrieveProblems
            .forDataset(id, pageable)
            .orElseThrow { DatasetNotFound(id) }

    @GetMapping("/api/datasets/{id}/problem/{problemId}/summary")
    fun getDatasetSummary(
        @PathVariable id: ThingId,
        @PathVariable problemId: ThingId,
        pageable: Pageable
    ): Page<DatasetSummary> =
        retrieveDatasets
            .summaryFor(id, problemId, pageable)
            .orElseThrow { DatasetNotFound(id) }
}
