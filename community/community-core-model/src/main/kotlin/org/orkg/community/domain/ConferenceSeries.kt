package org.orkg.community.domain

import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.OrganizationId
import java.time.LocalDate

data class ConferenceSeries(
    val id: ConferenceSeriesId,
    val organizationId: OrganizationId,
    var name: String,
    var homepage: String,
    @JsonProperty("display_id")
    var displayId: String,
    var metadata: Metadata,
)

data class Metadata(
    @JsonProperty("start_date")
    var startDate: LocalDate,
    @JsonProperty("review_process")
    var reviewType: PeerReviewType,
)

enum class PeerReviewType {
    SINGLE_BLIND,
    DOUBLE_BLIND,
    OPENREVIEW,
    ;

    companion object {
        fun fromOrNull(name: String): PeerReviewType? =
            entries.firstOrNull { it.name.equals(name, true) }
    }
}
