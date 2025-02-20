package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface BenchmarkUseCases : RetrieveBenchmarkUseCase

interface RetrieveBenchmarkUseCase {
    fun findAllBenchmarkSummariesByResearchFieldId(id: ThingId, pageable: Pageable): Optional<Page<BenchmarkSummary>>

    fun findAllBenchmarkSummaries(pageable: Pageable): Optional<Page<BenchmarkSummary>>
}
