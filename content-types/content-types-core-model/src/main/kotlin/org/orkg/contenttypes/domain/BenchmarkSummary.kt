package org.orkg.contenttypes.domain

data class BenchmarkSummary(
    val researchProblem: ResearchProblem,
    val researchFields: List<ResearchField> = emptyList(),
    val totalPapers: Int,
    val totalDatasets: Int,
    val totalCodes: Int,
)
