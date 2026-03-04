package org.orkg.contenttypes.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SmartReviewSnapshot
import org.orkg.contenttypes.domain.SmartReviewSnapshot.ModelVersion
import org.orkg.contenttypes.domain.SmartReviewSnapshotV1
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.graph.domain.GeneralStatement
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.treeToValue
import java.time.ZoneOffset

@Entity
@Table(name = "smart_review_snapshots")
class SmartReviewSnapshotEntity : SnapshotEntity<ModelVersion>() {
    @Column(name = "resource_id")
    var resourceId: String? = null

    @Column(name = "root_id")
    var rootId: String? = null

    fun toSmartReviewSnapshot(objectMapper: ObjectMapper): SmartReviewSnapshot<*> =
        when (modelVersion!!) {
            ModelVersion.V1 -> {
                SmartReviewSnapshotV1(
                    id = SnapshotId(id!!),
                    createdBy = ContributorId(createdBy!!),
                    createdAt = createdAt!!.withOffsetSameInstant(ZoneOffset.ofTotalSeconds(createdAtOffsetTotalSeconds!!)),
                    resourceId = ThingId(resourceId!!),
                    rootId = ThingId(rootId!!),
                    subgraph = objectMapper.treeToValue<List<GeneralStatement>>(data!!),
                )
            }
        }
}
