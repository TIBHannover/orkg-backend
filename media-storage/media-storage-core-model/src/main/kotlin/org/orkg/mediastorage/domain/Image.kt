package org.orkg.mediastorage.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import jakarta.activation.MimeType

data class Image(
    val id: ImageId,
    val data: ImageData,
    val mimeType: MimeType,
    val createdBy: ContributorId?,
    val createdAt: OffsetDateTime
)
