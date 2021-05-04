package eu.tib.orkg.prototype.paperswithcode.application.port.output

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResourceId

interface SummarizeBenchmarkQuery {
    fun byResearchField(id: ResourceId): List<BenchmarkSummary>
}

interface FindDatasetsQuery {
    fun forResearchProblem(id: ResourceId): List<Dataset>
}

interface SummarizeDatasetQuery {
    fun by(id: ResourceId): List<DatasetSummary>
}

interface FindResearchFieldsQuery {
    /**
     * Find all research fields that have benchmarks.
     *
     * @return This list of research fields, or an empty list otherwise.
     */
    fun withBenchmarks(): List<ResearchField>
}
