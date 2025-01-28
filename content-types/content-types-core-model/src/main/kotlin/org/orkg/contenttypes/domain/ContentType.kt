package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

sealed interface ContentType {
    val id: ThingId
    val createdAt: OffsetDateTime
    val createdBy: ContributorId
    val extractionMethod: ExtractionMethod
    val visibility: Visibility
    val unlistedBy: ContributorId?

    fun isOwnedBy(contributorId: ContributorId): Boolean = createdBy == contributorId
}
