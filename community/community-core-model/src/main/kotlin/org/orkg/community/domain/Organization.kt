package org.orkg.community.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.mediastorage.domain.ImageId

data class Organization(
    val id: OrganizationId?,

    var name: String?,

    @JsonProperty("created_by")
    val createdBy: ContributorId? = ContributorId.createUnknownContributor(),

    var homepage: String?,

    @JsonProperty("observatory_ids")
    val observatoryIds: Set<ObservatoryId> = emptySet(),

    @JsonProperty("display_id")
    var displayId: String?,

    var type: OrganizationType?,

    @JsonIgnore
    var logoId: ImageId?
)

enum class OrganizationType {
    GENERAL,
    CONFERENCE,
    JOURNAL;

    companion object {
        fun fromOrNull(name: String): OrganizationType? = values().firstOrNull { it.name.equals(name, true) }
    }
}
