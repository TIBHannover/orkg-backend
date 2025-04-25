package org.orkg.contenttypes.adapter.output.jpa.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PostgresTemplateBasedResourceSnapshotRepository : JpaRepository<TemplateBasedResourceSnapshotEntity, String> {
    fun findAllByResourceId(resourceId: String, pageable: Pageable): Page<TemplateBasedResourceSnapshotEntity>

    fun findAllByResourceIdAndTemplateId(
        resourceId: String,
        templateId: String,
        pageable: Pageable,
    ): Page<TemplateBasedResourceSnapshotEntity>
}
