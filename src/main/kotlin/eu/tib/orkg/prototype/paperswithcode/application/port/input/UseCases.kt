package eu.tib.orkg.prototype.paperswithcode.application.port.input

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional

interface RetrieveBenchmarkUseCase {
    fun summariesForResearchField(id: ResourceId): Optional<List<BenchmarkSummary>>
}

interface RetrieveDatasetUseCase {
    fun forResearchProblem(id: ResourceId): Optional<List<Dataset>>
    fun summaryFor(id: ResourceId, problemId: ResourceId): Optional<List<DatasetSummary>>
}

interface RetrieveResearchProblemsUseCase {
    fun forDataset(id: ResourceId): Optional<List<ResearchProblem>>
}

// TODO: Integrate with ResearchFieldService?
interface RetrieveResearchFieldUseCase {
    fun withBenchmarks(): List<ResearchField>
}
