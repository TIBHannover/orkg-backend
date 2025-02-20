package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.BenchmarkUseCases
import org.orkg.contenttypes.input.ResearchFieldUseCases
import org.orkg.contenttypes.output.SummarizeBenchmarkQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class BenchmarkService(
    private val summarizeBenchmark: SummarizeBenchmarkQuery,
    private val researchFieldService: ResearchFieldUseCases,
) : BenchmarkUseCases {
    override fun findAllBenchmarkSummariesByResearchFieldId(id: ThingId, pageable: Pageable): Optional<Page<BenchmarkSummary>> {
        val researchField = researchFieldService.findById(id)
        if (!researchField.isPresent) {
            return Optional.empty()
        }
        return Optional.of(
            summarizeBenchmark.findAllBenchmarkSummariesByResearchFieldId(researchField.get().id, pageable)
        )
    }

    override fun findAllBenchmarkSummaries(pageable: Pageable): Optional<Page<BenchmarkSummary>> =
        Optional.of(summarizeBenchmark.findAllBenchmarkSummaries(pageable))
}
