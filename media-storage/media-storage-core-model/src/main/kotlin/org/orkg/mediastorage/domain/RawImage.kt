package org.orkg.mediastorage.domain

import org.springframework.util.MimeType

data class RawImage(
    val data: ImageData,
    val mimeType: MimeType,
)
