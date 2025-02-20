package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility
import java.time.OffsetDateTime

sealed interface ContentType {
    val id: ThingId
    val createdAt: OffsetDateTime
    val createdBy: ContributorId
    val extractionMethod: ExtractionMethod
    val visibility: Visibility
    val unlistedBy: ContributorId?

    fun isOwnedBy(contributorId: ContributorId): Boolean = createdBy == contributorId
}
