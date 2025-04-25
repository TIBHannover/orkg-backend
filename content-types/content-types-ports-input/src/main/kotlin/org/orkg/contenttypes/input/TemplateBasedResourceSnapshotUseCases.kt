package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshot
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface TemplateBasedResourceSnapshotUseCases :
    RetrieveTemplateBasedResourceSnapshotUseCase,
    CreateTemplateBasedResourceSnapshotUseCase

interface RetrieveTemplateBasedResourceSnapshotUseCase {
    fun findById(snapshotId: SnapshotId): Optional<TemplateBasedResourceSnapshot<*>>

    fun findAllByResourceId(resourceId: ThingId, pageable: Pageable): Page<TemplateBasedResourceSnapshot<*>>

    fun findAllByResourceIdAndTemplateId(
        resourceId: ThingId,
        templateId: ThingId,
        pageable: Pageable,
    ): Page<TemplateBasedResourceSnapshot<*>>
}

interface CreateTemplateBasedResourceSnapshotUseCase {
    fun create(command: CreateCommand): SnapshotId

    data class CreateCommand(
        val resourceId: ThingId,
        val templateId: ThingId,
        val contributorId: ContributorId,
        val registerHandle: Boolean = true,
    )
}
