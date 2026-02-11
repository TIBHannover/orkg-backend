package org.orkg.contenttypes.adapter.output.jpa.internal

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.common.Handle
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshot
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshot.ModelVersion
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotV1
import org.orkg.contenttypes.domain.TemplateInstance
import java.time.ZoneOffset

@Entity
@Table(name = "template_based_resource_snapshots")
class TemplateBasedResourceSnapshotEntity : SnapshotEntity<ModelVersion>() {
    @Column(name = "resource_id", nullable = false)
    var resourceId: String? = null

    @Column(name = "template_id", nullable = false)
    var templateId: String? = null

    var handle: String? = null

    fun toTemplateBasedResourceSnapshot(objectMapper: ObjectMapper): TemplateBasedResourceSnapshot<*> =
        when (modelVersion!!) {
            ModelVersion.V1 -> {
                TemplateBasedResourceSnapshotV1(
                    id = SnapshotId(id!!),
                    createdBy = ContributorId(createdBy!!),
                    createdAt = createdAt!!.withOffsetSameInstant(ZoneOffset.ofTotalSeconds(createdAtOffsetTotalSeconds!!)),
                    data = objectMapper.treeToValue(data, TemplateInstance::class.java),
                    resourceId = ThingId(resourceId!!),
                    templateId = ThingId(templateId!!),
                    handle = handle?.let(Handle::of)
                )
            }
        }
}
