package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import java.time.OffsetDateTime

interface Snapshot<T : Any, V : Enum<V>> {
    val id: SnapshotId
    val createdBy: ContributorId
    val createdAt: OffsetDateTime
    val modelVersion: V
    val data: T
}
