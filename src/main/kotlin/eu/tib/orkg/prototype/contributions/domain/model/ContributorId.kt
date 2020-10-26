package eu.tib.orkg.prototype.contributions.domain.model

import java.util.UUID

data class ContributorId(val value: UUID) {
    companion object {
        fun createUnknownContributor() = ContributorId(UUID(0, 0))
    }
}
