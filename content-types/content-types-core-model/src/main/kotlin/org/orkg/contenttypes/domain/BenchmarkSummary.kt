package org.orkg.contenttypes.domain

import com.fasterxml.jackson.annotation.JsonProperty

/** Simple DTO for the benchmark response. */
data class BenchmarkSummary(
    @JsonProperty("research_problem")
    val researchProblem: ResearchProblem,
    @JsonProperty("research_fields")
    val researchFields: List<ResearchField> = emptyList(),
    override val totalPapers: Int,
    override val totalDatasets: Int,
    override val totalCodes: Int
) : PaperTotal, CodeTotal, DatasetTotal
