package eu.tib.orkg.prototype.paperswithcode.application.service

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveBenchmarkUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveDatasetUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindDatasetsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchProblemQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeBenchmarkQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeDatasetQuery
import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional
import org.springframework.stereotype.Service

@Service
class DummyBenchmarkService(
    private val findResearchProblem: FindResearchProblemQuery,
    private val summarizeBenchmark: SummarizeBenchmarkQuery,
    private val researchFieldService: ResearchFieldService
) : RetrieveBenchmarkUseCase {
    override fun summariesForResearchField(id: ResourceId): Optional<List<BenchmarkSummary>> {
        val researchField = researchFieldService.findById(id)
        if (!researchField.isPresent)
            return Optional.empty()
        // TODO: Might be better to do in one query?
        val benchmarkSummaries = findResearchProblem.allByResearchField(id).map {
            summarizeBenchmark.byResearchProblem(it.id).get()
        }
        return Optional.of(benchmarkSummaries)
    }
}

@Service
class DummyDatasetService(
    private val findDatasets: FindDatasetsQuery,
    private val researchProblemService: ProblemService,
    private val summarizeDataset: SummarizeDatasetQuery
) : RetrieveDatasetUseCase {
    override fun forResearchProblem(id: ResourceId): Optional<List<Dataset>> {
        val problem = researchProblemService.findById(id)
        if (!problem.isPresent) return Optional.empty()
        return findDatasets.forResearchProblem(id)
    }

    override fun summaryFor(id: ResourceId): Optional<List<DatasetSummary>> {
        // TODO: check that dataset exists
        return summarizeDataset.by(id)
    }
}

@Service
class DummyResearchFieldService(
    private val findResearchFields: FindResearchFieldsQuery
) : RetrieveResearchFieldUseCase {
    // FIXME: Integrate with ResearchFieldService?
    override fun withBenchmarks(): List<ResearchField> = findResearchFields.withBenchmarks()
}
