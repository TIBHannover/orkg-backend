package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.BenchmarkUseCases
import org.orkg.contenttypes.output.SummarizeBenchmarkQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BenchmarkService(
    private val summarizeBenchmark: SummarizeBenchmarkQuery,
) : BenchmarkUseCases {
    override fun findAllBenchmarkSummariesByResearchFieldId(id: ThingId, pageable: Pageable): Page<BenchmarkSummary> =
        summarizeBenchmark.findAllBenchmarkSummariesByResearchFieldId(id, pageable)

    override fun findAllBenchmarkSummaries(pageable: Pageable): Page<BenchmarkSummary> =
        summarizeBenchmark.findAllBenchmarkSummaries(pageable)
}
