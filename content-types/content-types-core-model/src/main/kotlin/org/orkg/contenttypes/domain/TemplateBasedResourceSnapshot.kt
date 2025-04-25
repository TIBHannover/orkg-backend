package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.Handle
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshot.ModelVersion
import java.time.OffsetDateTime

sealed interface TemplateBasedResourceSnapshot<T : Any> : Snapshot<T, ModelVersion> {
    val templateInstance: TemplateInstance
    val resourceId: ThingId
    val templateId: ThingId
    val handle: Handle?

    enum class ModelVersion {
        V1,
    }
}

data class TemplateBasedResourceSnapshotV1(
    override val id: SnapshotId,
    override val createdBy: ContributorId,
    override val createdAt: OffsetDateTime,
    override val data: TemplateInstance,
    override val resourceId: ThingId,
    override val templateId: ThingId,
    override val handle: Handle?,
) : TemplateBasedResourceSnapshot<TemplateInstance> {
    override val templateInstance: TemplateInstance = data
    override val modelVersion: ModelVersion = ModelVersion.V1
}
