package org.orkg.contenttypes.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteratureListSnapshot
import org.orkg.contenttypes.domain.LiteratureListSnapshot.ModelVersion
import org.orkg.contenttypes.domain.LiteratureListSnapshotV1
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.graph.domain.GeneralStatement
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.treeToValue
import java.time.ZoneOffset

@Entity
@Table(name = "literature_list_snapshots")
class LiteratureListSnapshotEntity : SnapshotEntity<ModelVersion>() {
    @Column(name = "resource_id", nullable = false)
    var resourceId: String? = null

    @Column(name = "root_id", nullable = false)
    var rootId: String? = null

    fun toLiteratureListSnapshot(objectMapper: ObjectMapper): LiteratureListSnapshot<*> =
        when (modelVersion!!) {
            ModelVersion.V1 -> {
                LiteratureListSnapshotV1(
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
