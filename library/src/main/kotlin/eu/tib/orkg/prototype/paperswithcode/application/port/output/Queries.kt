package eu.tib.orkg.prototype.paperswithcode.application.port.output

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SummarizeBenchmarkQuery {
    fun byResearchField(id: ThingId, pageable: Pageable): Page<BenchmarkSummary>
    fun getAll(pageable: Pageable): Page<BenchmarkSummary>
}

interface FindDatasetsQuery {
    fun forResearchProblem(id: ThingId, pageable: Pageable): Page<Dataset>
}

interface SummarizeDatasetQuery {
    fun by(id: ThingId, pageable: Pageable): Page<DatasetSummary>
    fun byAndProblem(id: ThingId, problemId: ThingId, pageable: Pageable): Page<DatasetSummary>
}

interface FindResearchFieldsQuery {
    /**
     * Find all research fields that have benchmarks.
     *
     * @return This list of research fields, or an empty list otherwise.
     */
    fun withBenchmarks(pageable: Pageable): Page<ResearchField>
}

interface FindResearchProblemQuery {
    fun findResearchProblemForDataset(datasetId: ThingId, pageable: Pageable): Page<ResearchProblem>
}
