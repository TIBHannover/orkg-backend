package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing

data class VersionInfo(
    val head: HeadVersion,
    val published: List<PublishedVersion>
)

data class HeadVersion(
    val id: ThingId,
    val label: String,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId
) {
    constructor(thing: Thing) :
        this(thing.id, thing.label, thing.createdAt, thing.createdBy)
}

data class PublishedVersion(
    val id: ThingId,
    val label: String,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val changelog: String?
) {
    constructor(thing: Thing, description: String?) :
        this(thing.id, thing.label, thing.createdAt, thing.createdBy, description)
}
