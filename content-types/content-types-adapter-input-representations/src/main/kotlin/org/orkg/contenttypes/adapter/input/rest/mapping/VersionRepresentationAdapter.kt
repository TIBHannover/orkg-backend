package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.VersionInfoRepresentation
import org.orkg.contenttypes.adapter.input.rest.HeadVersionRepresentation
import org.orkg.contenttypes.adapter.input.rest.PublishedVersionRepresentation
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.PublishedVersion
import org.springframework.data.domain.Page

interface VersionRepresentationAdapter {

    fun Optional<VersionInfo>.mapToVersionInfoRepresentation(): Optional<VersionInfoRepresentation> =
        map { it.toVersionInfoRepresentation() }

    fun Page<VersionInfo>.mapToVersionInfoRepresentation(): Page<VersionInfoRepresentation> =
        map { it.toVersionInfoRepresentation() }

    fun VersionInfo.toVersionInfoRepresentation(): VersionInfoRepresentation =
        VersionInfoRepresentation(
            head = head.toHeadVersionRepresentation(),
            published = published.map { it.toPublishedVersionRepresentation() }
        )

    fun Optional<HeadVersion>.mapToHeadVersionRepresentation(): Optional<HeadVersionRepresentation> =
        map { it.toHeadVersionRepresentation() }

    fun Page<HeadVersion>.mapToHeadVersionRepresentation(): Page<HeadVersionRepresentation> =
        map { it.toHeadVersionRepresentation() }

    fun HeadVersion.toHeadVersionRepresentation(): HeadVersionRepresentation =
        HeadVersionRepresentation(id, label, createdAt, createdBy)

    fun Optional<PublishedVersion>.mapToPublishedVersionRepresentation(): Optional<PublishedVersionRepresentation> =
        map { it.toPublishedVersionRepresentation() }

    fun Page<PublishedVersion>.mapToPublishedVersionRepresentation(): Page<PublishedVersionRepresentation> =
        map { it.toPublishedVersionRepresentation() }

    fun PublishedVersion.toPublishedVersionRepresentation(): PublishedVersionRepresentation =
        PublishedVersionRepresentation(id, label, createdAt, createdBy, changelog)
}
