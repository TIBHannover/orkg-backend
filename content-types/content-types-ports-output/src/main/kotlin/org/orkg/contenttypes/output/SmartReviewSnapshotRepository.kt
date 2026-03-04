package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReviewSnapshot
import org.orkg.contenttypes.domain.SnapshotId
import java.util.Optional

interface SmartReviewSnapshotRepository {
    fun save(snapshot: SmartReviewSnapshot<*>)

    fun findById(id: SnapshotId): Optional<SmartReviewSnapshot<*>>

    fun findByResourceId(id: ThingId): Optional<SmartReviewSnapshot<*>>

    fun deleteAll()

    fun count(): Long
}
