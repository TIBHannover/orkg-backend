package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BenchmarkUseCases : RetrieveBenchmarkUseCase

interface RetrieveBenchmarkUseCase {
    fun findAllBenchmarkSummariesByResearchFieldId(id: ThingId, pageable: Pageable): Page<BenchmarkSummary>

    fun findAllBenchmarkSummaries(pageable: Pageable): Page<BenchmarkSummary>
}
