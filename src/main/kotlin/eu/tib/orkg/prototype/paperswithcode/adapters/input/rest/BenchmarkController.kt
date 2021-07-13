package eu.tib.orkg.prototype.paperswithcode.adapters.input.rest

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveBenchmarkUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveDatasetUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveResearchProblemsUseCase
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.application.DatasetNotFound
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.application.ResearchProblemNotFound
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class BenchmarkController(
    private val retrieveResearchField: RetrieveResearchFieldUseCase,
    private val retrieveBenchmarks: RetrieveBenchmarkUseCase,
    private val retrieveDatasets: RetrieveDatasetUseCase,
    private val retrieveProblems: RetrieveResearchProblemsUseCase
) {
    @GetMapping("/api/research-fields/benchmarks")
    fun getResearchFieldsWithBenchmarks(): List<ResearchField> =
        retrieveResearchField.withBenchmarks()

    @GetMapping("/api/benchmarks/summary/research-field/{id}")
    fun getBenchmarkSummaryForResearchField(@PathVariable id: ResourceId): List<BenchmarkSummary> =
        retrieveBenchmarks
            .summariesForResearchField(id)
            .orElseThrow { ResearchFieldNotFound(id) }

    @GetMapping("/api/datasets/research-problem/{id}")
    fun getDatasetForResearchProblem(@PathVariable id: ResourceId): List<Dataset> =
        retrieveDatasets
            .forResearchProblem(id)
            .orElseThrow { ResearchProblemNotFound(id) }

    @GetMapping("/api/datasets/{id}/problems")
    fun getResearchProblemsForDataset(@PathVariable id: ResourceId): List<ResearchProblem> =
        retrieveProblems
            .forDataset(id)
            .orElseThrow { DatasetNotFound(id) }

    @GetMapping("/api/datasets/{id}/problem/{problemId}/summary")
    fun getDatasetSummary(
        @PathVariable id: ResourceId,
        @PathVariable problemId: ResourceId
    ): List<DatasetSummary> =
        retrieveDatasets
            .summaryFor(id, problemId)
            .orElseThrow { DatasetNotFound(id) }
}
