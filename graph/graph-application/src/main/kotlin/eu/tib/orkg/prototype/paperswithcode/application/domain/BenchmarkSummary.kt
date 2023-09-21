package eu.tib.orkg.prototype.paperswithcode.application.domain

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem

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