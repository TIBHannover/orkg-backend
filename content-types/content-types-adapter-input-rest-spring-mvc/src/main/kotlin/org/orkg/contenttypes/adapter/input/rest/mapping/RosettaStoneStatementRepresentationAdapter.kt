package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneStatementRepresentation
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.contenttypes.domain.RosettaStoneStatementVersionNotFound
import org.springframework.data.domain.Page

interface RosettaStoneStatementRepresentationAdapter : ThingReferenceRepresentationAdapter {

    fun Optional<RosettaStoneStatement>.mapToRosettaStoneStatementRepresentation(
        versionId: ThingId
    ): Optional<RosettaStoneStatementRepresentation> = map { rosettaStoneStatement ->
        rosettaStoneStatement.findVersionById(versionId)
            ?.let { rosettaStoneStatement.toRosettaStoneStatementRepresentation(versionId, it) }
            ?: throw RosettaStoneStatementVersionNotFound(versionId)
    }

    fun Optional<RosettaStoneStatement>.mapToRosettaStoneStatementRepresentation(): Optional<List<RosettaStoneStatementRepresentation>> =
        map { rosettaStoneStatement -> rosettaStoneStatement.versions.map { rosettaStoneStatement.toRosettaStoneStatementRepresentation(it.id, it) } }

    fun Page<RosettaStoneStatement>.mapToRosettaStoneStatementRepresentation(): Page<RosettaStoneStatementRepresentation> =
        map { it.toRosettaStoneStatementRepresentation(it.id, it.latestVersion) }

    private fun RosettaStoneStatement.toRosettaStoneStatementRepresentation(
        requestedVersionId: ThingId,
        version: RosettaStoneStatementVersion
    ): RosettaStoneStatementRepresentation =
        RosettaStoneStatementRepresentation(
            id = requestedVersionId,
            context = contextId,
            templateId = templateId,
            classId = templateTargetClassId,
            versionId = version.id,
            latestVersion = id,
            formattedLabel = version.formattedLabel.value,
            subjects = version.subjects.map { it.toThingReferenceRepresentation() },
            objects = version.objects.map { objects -> objects.map { it.toThingReferenceRepresentation() } },
            createdAt = version.createdAt,
            createdBy = version.createdBy,
            certainty = version.certainty,
            negated = version.negated,
            observatories = version.observatories,
            organizations = version.organizations,
            extractionMethod = version.extractionMethod,
            visibility = version.visibility,
            unlistedBy = version.unlistedBy,
            modifiable = modifiable,
            deletedBy = version.deletedBy,
            deletedAt = version.deletedAt
        )
}
