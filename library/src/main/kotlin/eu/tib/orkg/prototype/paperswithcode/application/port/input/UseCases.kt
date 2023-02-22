package eu.tib.orkg.prototype.paperswithcode.application.port.input

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.Optional

interface RetrieveBenchmarkUseCase {
    fun summariesForResearchField(id: ThingId): Optional<List<BenchmarkSummary>>
    fun summary(): Optional<List<BenchmarkSummary>>
}

interface RetrieveDatasetUseCase {
    fun forResearchProblem(id: ThingId): Optional<List<Dataset>>
    fun summaryFor(id: ThingId, problemId: ThingId): Optional<List<DatasetSummary>>
}
