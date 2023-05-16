package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

/** Simple DTO for the contribution response. In specific the header of the contribution-comparison */
data class ContributionInfo(
    val id: ResourceId,
    val label: String,
    @JsonProperty("paper_title")
    val paperTitle: String,
    @JsonProperty("paper_year")
    val paperYear: Int?,
    @JsonProperty("paper_id")
    val paperId: ResourceId
)
