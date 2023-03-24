package eu.tib.orkg.prototype.paperswithcode.application.port.input

import eu.tib.orkg.prototype.paperswithcode.application.domain.BenchmarkSummary
import eu.tib.orkg.prototype.paperswithcode.application.domain.Dataset
import eu.tib.orkg.prototype.paperswithcode.application.domain.DatasetSummary
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveBenchmarkUseCase {
    fun summariesForResearchField(id: ThingId, pageable: Pageable): Optional<Page<BenchmarkSummary>>
    fun summary(pageable: Pageable): Optional<Page<BenchmarkSummary>>
}

interface RetrieveDatasetUseCase {
    fun forResearchProblem(id: ThingId, pageable: Pageable): Optional<Page<Dataset>>
    fun summaryFor(id: ThingId, problemId: ThingId, pageable: Pageable): Optional<Page<DatasetSummary>>
}
