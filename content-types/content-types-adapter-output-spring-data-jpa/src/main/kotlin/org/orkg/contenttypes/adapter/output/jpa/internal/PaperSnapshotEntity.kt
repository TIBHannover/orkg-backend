package org.orkg.contenttypes.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperSnapshot
import org.orkg.contenttypes.domain.PaperSnapshot.ModelVersion
import org.orkg.contenttypes.domain.PaperSnapshotV1
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.graph.domain.GeneralStatement
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.treeToValue
import java.time.ZoneOffset

@Entity
@Table(name = "paper_snapshots")
class PaperSnapshotEntity : SnapshotEntity<ModelVersion>() {
    @Column(name = "resource_id", nullable = false)
    var resourceId: String? = null

    fun toPaperSnapshot(objectMapper: ObjectMapper): PaperSnapshot<*> =
        when (modelVersion!!) {
            ModelVersion.V1 -> {
                PaperSnapshotV1(
                    id = SnapshotId(id!!),
                    createdBy = ContributorId(createdBy!!),
                    createdAt = createdAt!!.withOffsetSameInstant(ZoneOffset.ofTotalSeconds(createdAtOffsetTotalSeconds!!)),
                    resourceId = ThingId(resourceId!!),
                    subgraph = objectMapper.treeToValue<List<GeneralStatement>>(data!!),
                )
            }
        }
}
