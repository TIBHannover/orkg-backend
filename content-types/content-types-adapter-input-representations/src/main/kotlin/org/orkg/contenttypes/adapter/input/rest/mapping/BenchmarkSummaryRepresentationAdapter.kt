package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.BenchmarkSummaryRepresentation
import org.orkg.contenttypes.domain.BenchmarkSummary
import org.springframework.data.domain.Page

interface BenchmarkSummaryRepresentationAdapter {
    fun Page<BenchmarkSummary>.mapToBenchmarkSummaryRepresentation(): Page<BenchmarkSummaryRepresentation> =
        map { it.toBenchmarkSummaryRepresentation() }

    fun BenchmarkSummary.toBenchmarkSummaryRepresentation(): BenchmarkSummaryRepresentation =
        BenchmarkSummaryRepresentation(researchProblem, researchFields, totalPapers, totalDatasets, totalCodes)
}
