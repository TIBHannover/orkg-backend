package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteratureListSnapshot.ModelVersion
import org.orkg.graph.domain.GeneralStatement
import java.time.OffsetDateTime

sealed interface LiteratureListSnapshot<T : Any> : Snapshot<T, ModelVersion> {
    val resourceId: ThingId
    val rootId: ThingId
    val subgraph: List<GeneralStatement>

    enum class ModelVersion {
        V1,
    }
}

data class LiteratureListSnapshotV1(
    override val id: SnapshotId,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    override val resourceId: ThingId,
    override val rootId: ThingId,
    override val subgraph: List<GeneralStatement>,
) : LiteratureListSnapshot<List<GeneralStatement>> {
    override val data: List<GeneralStatement> = subgraph
    override val modelVersion: ModelVersion = ModelVersion.V1
}
