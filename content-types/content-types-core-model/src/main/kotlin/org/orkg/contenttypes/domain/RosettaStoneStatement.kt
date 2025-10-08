package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.DynamicLabel
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import java.time.OffsetDateTime

data class RosettaStoneStatement(
    val id: ThingId,
    val contextId: ThingId?,
    val templateId: ThingId,
    val templateTargetClassId: ThingId,
    val label: String,
    val versions: List<RosettaStoneStatementVersion>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val visibility: Visibility,
    val modifiable: Boolean,
    val unlistedBy: ContributorId? = null,
) {
    init {
        require(versions.isNotEmpty()) { "Must have at least one version." }
        require(observatories.size <= 1) { "Must be at most one observatory." }
        require(organizations.size <= 1) { "Must be at most one organization." }
    }

    val latestVersion = versions.last()

    fun findVersionById(versionId: ThingId): RosettaStoneStatementVersion? {
        if (versionId == id) {
            return latestVersion
        }
        return versions.find { it.id == versionId }
    }

    fun withVersion(version: RosettaStoneStatementVersion): RosettaStoneStatement =
        copy(versions = versions + version)
}

data class RosettaStoneStatementVersion(
    val id: ThingId,
    val label: String,
    val dynamicLabel: DynamicLabel,
    val subjects: List<Thing>,
    val objects: List<List<Thing>>,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val certainty: Certainty,
    val negated: Boolean,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val visibility: Visibility,
    val modifiable: Boolean,
    val unlistedBy: ContributorId? = null,
    val deletedBy: ContributorId? = null,
    val deletedAt: OffsetDateTime? = null,
) {
    init {
        require(subjects.isNotEmpty()) { "Must have at least one subject." }
        require(observatories.size <= 1) { "Must be at most one observatory." }
        require(organizations.size <= 1) { "Must be at most one organization." }
    }

    val allInputs: Set<Thing> get() = subjects union objects.flatten()
}

enum class Certainty {
    LOW,
    MODERATE,
    HIGH,
}
