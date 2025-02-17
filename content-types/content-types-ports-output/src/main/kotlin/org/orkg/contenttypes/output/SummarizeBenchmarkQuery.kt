package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SummarizeBenchmarkQuery {
    fun findAllBenchmarkSummariesByResearchFieldId(id: ThingId, pageable: Pageable): Page<BenchmarkSummary>
    fun findAllBenchmarkSummaries(pageable: Pageable): Page<BenchmarkSummary>
}
