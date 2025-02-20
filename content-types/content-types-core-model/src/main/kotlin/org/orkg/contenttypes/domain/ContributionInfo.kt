package org.orkg.contenttypes.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ThingId

/** Simple DTO for the contribution response. In specific the header of the contribution-comparison */
data class ContributionInfo(
    val id: ThingId,
    val label: String,
    @JsonProperty("paper_title")
    val paperTitle: String,
    @JsonProperty("paper_year")
    val paperYear: Int?,
    @JsonProperty("paper_id")
    val paperId: ThingId,
)
