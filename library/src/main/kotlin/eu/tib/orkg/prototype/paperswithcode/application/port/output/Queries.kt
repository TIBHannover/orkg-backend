package eu.tib.orkg.prototype.paperswithcode.application.port.output

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResourceId

interface SummarizeBenchmarkQuery {
    fun byResearchField(id: ResourceId): List<BenchmarkSummary>
    fun getAll(): List<BenchmarkSummary>
}

interface FindDatasetsQuery {
    fun forResearchProblem(id: ResourceId): List<Dataset>
}

interface SummarizeDatasetQuery {
    fun by(id: ResourceId): List<DatasetSummary>
    fun byAndProblem(id: ResourceId, problemId: ResourceId): List<DatasetSummary>
}

interface FindResearchFieldsQuery {
    /**
     * Find all research fields that have benchmarks.
     *
     * @return This list of research fields, or an empty list otherwise.
     */
    fun withBenchmarks(): List<ResearchField>
}

interface FindResearchProblemQuery {
    fun findResearchProblemForDataset(datasetId: ResourceId): List<ResearchProblem>
}
