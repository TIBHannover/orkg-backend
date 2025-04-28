package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.common.GoogleScholarId
import org.orkg.common.IdentifierValue
import org.orkg.common.LinkedInId
import org.orkg.common.OpenAlexId
import org.orkg.common.ResearchGateId
import org.orkg.common.ResearcherId
import org.orkg.common.WikidataId
import java.time.OffsetDateTime

data class ContributorIdentifier(
    val contributorId: ContributorId,
    val type: Type,
    val value: IdentifierValue,
    val createdAt: OffsetDateTime,
) {
    enum class Type(
        val id: String,
        val newInstance: (String) -> IdentifierValue,
    ) {
        ORCID("orcid", org.orkg.common.ORCID::of),
        GOOGLE_SCHOLAR_ID("google_scholar", GoogleScholarId::of),
        RESEARCH_GATE_ID("research_gate", ResearchGateId::of),
        LINKED_IN_ID("linked_in", LinkedInId::of),
        WIKIDATA_ID("wikidata", WikidataId::of),
        RESEARCHER_ID("web_of_science", ResearcherId::of),
        OPEN_ALEX("open_alex", OpenAlexId::of),
        ;

        companion object {
            fun byId(id: String): Type? =
                entries.firstOrNull { it.id == id }
        }
    }

    // Override because IdentifierValue is not comparable.
    // For more info see https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1304#note_2469005292
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContributorIdentifier

        if (contributorId != other.contributorId) return false
        if (type != other.type) return false
        if (value.value != other.value.value) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    // Override because IdentifierValue is not comparable.
    // For more info see https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1304#note_2469005292
    override fun hashCode(): Int {
        var result = contributorId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + value.value.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
