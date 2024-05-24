package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneStatementRepresentation
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.contenttypes.domain.RosettaStoneStatementVersionNotFound
import org.orkg.contenttypes.domain.ThingReference
import org.springframework.data.domain.Page

interface RosettaStoneStatementRepresentationAdapter {

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
            templateId = templateId,
            versionId = version.id,
            latestVersion = id,
            context = contextId,
            subjects = version.subjects.map(ThingReference::from),
            objects = version.objects.map { it.map(ThingReference::from) },
            createdAt = version.createdAt,
            createdBy = version.createdBy,
            certainty = version.certainty,
            negated = version.negated,
            observatories = version.observatories,
            organizations = version.organizations,
            extractionMethod = version.extractionMethod,
            visibility = version.visibility,
            unlistedBy = version.unlistedBy,
            modifiable = modifiable
        )
}
