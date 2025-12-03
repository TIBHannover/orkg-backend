package org.orkg.contenttypes.adapter.input.rest

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.mapping.BenchmarkSummaryRepresentationAdapter
import org.orkg.contenttypes.domain.ResearchField
import org.orkg.contenttypes.input.BenchmarkUseCases
import org.orkg.contenttypes.input.LegacyResearchFieldUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class BenchmarkController(
    private val retrieveResearchField: LegacyResearchFieldUseCases,
    private val retrieveBenchmarks: BenchmarkUseCases,
) : BenchmarkSummaryRepresentationAdapter {
    @GetMapping("/api/research-fields/benchmarks")
    fun findAllWithBenchmarks(pageable: Pageable): Page<ResearchField> =
        retrieveResearchField.findAllWithBenchmarks(pageable)

    @GetMapping("/api/benchmarks/summary/research-field/{id}")
    fun findAllBenchmarkSummariesByResearchFieldId(
        @PathVariable id: ThingId,
        pageable: Pageable,
    ): Page<BenchmarkSummaryRepresentation> =
        retrieveBenchmarks.findAllBenchmarkSummariesByResearchFieldId(id, pageable)
            .mapToBenchmarkSummaryRepresentation()

    @GetMapping("/api/benchmarks/summary")
    fun findAllBenchmarkSummaries(pageable: Pageable): Page<BenchmarkSummaryRepresentation> =
        retrieveBenchmarks.findAllBenchmarkSummaries(pageable)
            .mapToBenchmarkSummaryRepresentation()
}
