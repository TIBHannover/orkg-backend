package org.orkg.contenttypes.adapter.output.jpa.internal

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PostgresSmartReviewSnapshotRepository : JpaRepository<SmartReviewSnapshotEntity, String> {
    fun findByResourceId(resourceId: String): Optional<SmartReviewSnapshotEntity>
}
