package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshot
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface TemplateBasedResourceSnapshotRepository {
    fun save(snapshot: TemplateBasedResourceSnapshot<*>)

    fun findById(id: SnapshotId): Optional<TemplateBasedResourceSnapshot<*>>

    fun findAllByResourceId(resourceId: ThingId, pageable: Pageable): Page<TemplateBasedResourceSnapshot<*>>

    fun findAllByResourceIdAndTemplateId(
        resourceId: ThingId,
        templateId: ThingId,
        pageable: Pageable,
    ): Page<TemplateBasedResourceSnapshot<*>>

    fun deleteAll()

    fun count(): Long
}
