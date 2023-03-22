package eu.tib.orkg.prototype.community.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.ImageId
import java.util.*

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
    val logoId: ImageId?
)

enum class OrganizationType {
    GENERAL,
    CONFERENCE,
    JOURNAL;

    companion object {
        fun fromOrNull(name: String): OrganizationType? = values().firstOrNull { it.name.equals(name, true) }
    }
}
