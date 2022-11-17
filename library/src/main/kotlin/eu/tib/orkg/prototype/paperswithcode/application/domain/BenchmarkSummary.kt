package eu.tib.orkg.prototype.paperswithcode.application.domain

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ResearchField

/** Simple DTO for the benchmark response. */
data class BenchmarkSummary(
    @JsonProperty("research_problem")
    val researchProblem: ResearchProblem,
    @JsonProperty("research_field")
    // TODO: Default value for legacy PwC model. Remove and make non-nullable after migration. Also update API doc.
    val researchField: ResearchField? = null,
    @JsonProperty("research_fields")
    val researchFields: List<ResearchField> = emptyList(),
    override val totalPapers: Int,
    override val totalDatasets: Int,
    override val totalCodes: Int
) : PaperTotal, CodeTotal, DatasetTotal
