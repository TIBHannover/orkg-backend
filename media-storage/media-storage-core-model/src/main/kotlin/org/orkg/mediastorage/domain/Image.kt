package org.orkg.mediastorage.domain

import org.orkg.common.ContributorId
import org.springframework.util.MimeType
import java.time.OffsetDateTime

data class Image(
    val id: ImageId,
    val data: ImageData,
    val mimeType: MimeType,
    val createdBy: ContributorId?,
    val createdAt: OffsetDateTime,
)
