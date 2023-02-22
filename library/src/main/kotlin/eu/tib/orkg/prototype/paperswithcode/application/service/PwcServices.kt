package eu.tib.orkg.prototype.paperswithcode.application.service

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveBenchmarkUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveDatasetUseCase
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindDatasetsQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeBenchmarkQuery
import eu.tib.orkg.prototype.paperswithcode.application.port.output.SummarizeDatasetQuery
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Primary
@Service
class BenchmarkService(
    private val summarizeBenchmark: SummarizeBenchmarkQuery,
    private val researchFieldService: RetrieveResearchFieldUseCase,
    private val flags: FeatureFlagService,
) : RetrieveBenchmarkUseCase {
    override fun summariesForResearchField(id: ThingId): Optional<List<BenchmarkSummary>> {
        val researchField = researchFieldService.findById(id)
        if (!researchField.isPresent)
            return Optional.empty()
        return Optional.of(
            summarizeBenchmark.byResearchField(researchField.get().id)
        )
    }

    override fun summary(): Optional<List<BenchmarkSummary>> = if (flags.isPapersWithCodeLegacyModelEnabled())
        error("This method is not supported in the PwC legacy model! Calling it is a bug!")
    else
        Optional.of(summarizeBenchmark.getAll())
}

@Primary
@Service
class DatasetService(
    private val findDatasets: FindDatasetsQuery,
    private val researchProblemService: RetrieveResearchProblemUseCase,
    private val summarizeDataset: SummarizeDatasetQuery,
    private val resourceService: ResourceUseCases
) : RetrieveDatasetUseCase {
    override fun forResearchProblem(id: ThingId): Optional<List<Dataset>> {
        val problem = researchProblemService.findById(id)
        if (!problem.isPresent) return Optional.empty()
        return Optional.of(findDatasets.forResearchProblem(id))
    }

    override fun summaryFor(id: ThingId, problemId: ThingId): Optional<List<DatasetSummary>> {
        val dataset = resourceService.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        val problem = resourceService.findById(problemId)
        if (!problem.isPresent) return Optional.empty()
        return Optional.of(summarizeDataset.byAndProblem(id, problemId))
    }
}
