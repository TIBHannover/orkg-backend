package eu.tib.orkg.prototype.paperswithcode.application.port.output

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.Optional

interface FindResearchProblemQuery {
    fun allByResearchField(id: ResourceId): List<ResearchProblem>
}

interface SummarizeBenchmarkQuery {
    fun byResearchProblem(id: ResourceId): Optional<BenchmarkSummary>
}

interface FindDatasetsQuery {
    fun forResearchProblem(id: ResourceId): Optional<List<Dataset>>
}

interface SummarizeDatasetQuery {
    fun by(id: ResourceId): Optional<List<DatasetSummary>>
}

interface FindResearchFieldsQuery {
    /**
     * Find all research fields that have benchmarks.
     *
     * @return This list of research fields, or an empty list otherwise.
     */
    fun withBenchmarks(): List<ResearchField>
}
