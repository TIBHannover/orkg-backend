package eu.tib.orkg.prototype.paperswithcode.adapters.input.rest

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveBenchmarkUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveDatasetUseCase
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase
import eu.tib.orkg.prototype.statements.application.DatasetNotFound
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.application.ResearchProblemNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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
