package org.orkg.mediastorage.domain

import java.time.OffsetDateTime
import javax.activation.MimeType
import org.orkg.common.ContributorId

data class Image(
    val id: ImageId,
    val data: ImageData,
    val mimeType: MimeType,
    val createdBy: ContributorId?,
    val createdAt: OffsetDateTime
)
