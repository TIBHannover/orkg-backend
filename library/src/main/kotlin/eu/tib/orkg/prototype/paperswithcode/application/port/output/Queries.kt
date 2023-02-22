package eu.tib.orkg.prototype.paperswithcode.application.port.output

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface SummarizeBenchmarkQuery {
    fun byResearchField(id: ThingId): List<BenchmarkSummary>
    fun getAll(): List<BenchmarkSummary>
}

interface FindDatasetsQuery {
    fun forResearchProblem(id: ThingId): List<Dataset>
}

interface SummarizeDatasetQuery {
    fun by(id: ThingId): List<DatasetSummary>
    fun byAndProblem(id: ThingId, problemId: ThingId): List<DatasetSummary>
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
    fun findResearchProblemForDataset(datasetId: ThingId): List<ResearchProblem>
}
